package com.logisticsapplication.service.impl;

import com.logisticsapplication.dto.response.AsyncShipmentTaskStatusResponse;
import com.logisticsapplication.dto.response.ShipmentResponse;
import com.logisticsapplication.model.AsyncTaskStatus;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AsyncShipmentTaskRegistry {

    private final AtomicLong taskIdSequence = new AtomicLong();
    private final ConcurrentMap<Long, AsyncShipmentTaskSnapshot> tasks =
            new ConcurrentHashMap<>();

    public AsyncShipmentTaskStatusResponse createTask(int requestedShipments) {
        long taskId = taskIdSequence.incrementAndGet();
        AsyncShipmentTaskSnapshot snapshot = new AsyncShipmentTaskSnapshot(
                taskId,
                AsyncTaskStatus.PENDING,
                requestedShipments,
                0,
                List.of(),
                null,
                Instant.now(),
                null,
                null
        );
        tasks.put(taskId, snapshot);
        return snapshot.toResponse();
    }

    public void markRunning(Long taskId) {
        updateTask(taskId, previous -> previous.withStatus(AsyncTaskStatus.RUNNING, Instant.now()));
    }

    public void markCompleted(Long taskId, List<ShipmentResponse> createdShipments) {
        updateTask(
                taskId,
                previous -> previous.withCompletion(
                        AsyncTaskStatus.COMPLETED,
                        createdShipments.size(),
                        createdShipments.stream()
                                .map(ShipmentResponse::getId)
                                .toList(),
                        null,
                        Instant.now()
                )
        );
    }

    public void markFailed(Long taskId, String errorMessage) {
        updateTask(
                taskId,
                previous -> previous.withCompletion(
                        AsyncTaskStatus.FAILED,
                        0,
                        List.of(),
                        errorMessage,
                        Instant.now()
                )
        );
    }

    public AsyncShipmentTaskStatusResponse getTask(Long taskId) {
        AsyncShipmentTaskSnapshot snapshot = tasks.get(taskId);
        if (snapshot == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Async task not found: " + taskId
            );
        }
        return snapshot.toResponse();
    }

    private void updateTask(Long taskId, TaskSnapshotUpdater updater) {
        tasks.compute(
                taskId,
                (ignored, existing) -> {
                    if (existing == null) {
                        throw new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Async task not found: " + taskId
                        );
                    }
                    return updater.update(existing);
                }
        );
    }

    @FunctionalInterface
    private interface TaskSnapshotUpdater {

        AsyncShipmentTaskSnapshot update(AsyncShipmentTaskSnapshot snapshot);
    }

    private record AsyncShipmentTaskSnapshot(
            Long taskId,
            AsyncTaskStatus status,
            int requestedShipments,
            int processedShipments,
            List<Long> createdShipmentIds,
            String errorMessage,
            Instant createdAt,
            Instant startedAt,
            Instant completedAt
    ) {

        private AsyncShipmentTaskSnapshot withStatus(
                AsyncTaskStatus newStatus,
                Instant newStartedAt
        ) {
            return new AsyncShipmentTaskSnapshot(
                    taskId,
                    newStatus,
                    requestedShipments,
                    processedShipments,
                    createdShipmentIds,
                    errorMessage,
                    createdAt,
                    startedAt == null ? newStartedAt : startedAt,
                    completedAt
            );
        }

        private AsyncShipmentTaskSnapshot withCompletion(
                AsyncTaskStatus newStatus,
                int newProcessedShipments,
                List<Long> newCreatedShipmentIds,
                String newErrorMessage,
                Instant newCompletedAt
        ) {
            return new AsyncShipmentTaskSnapshot(
                    taskId,
                    newStatus,
                    requestedShipments,
                    newProcessedShipments,
                    newCreatedShipmentIds,
                    newErrorMessage,
                    createdAt,
                    startedAt == null ? createdAt : startedAt,
                    newCompletedAt
            );
        }

        private AsyncShipmentTaskStatusResponse toResponse() {
            return new AsyncShipmentTaskStatusResponse(
                    taskId,
                    status,
                    requestedShipments,
                    processedShipments,
                    createdShipmentIds,
                    errorMessage,
                    createdAt,
                    startedAt,
                    completedAt
            );
        }
    }
}
