package com.logisticsapplication.service.impl;

import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.response.ShipmentResponse;
import com.logisticsapplication.service.ShipmentService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ShipmentAsyncWorker {

    private static final long SIMULATED_DELAY_MILLIS = 10000L;

    private final ShipmentService shipmentService;
    private final AsyncShipmentTaskRegistry taskRegistry;

    public ShipmentAsyncWorker(
            ShipmentService shipmentService,
            AsyncShipmentTaskRegistry taskRegistry
    ) {
        this.shipmentService = shipmentService;
        this.taskRegistry = taskRegistry;
    }

    @Async("shipmentTaskExecutor")
    public CompletableFuture<Void> processBulkCreation(
            Long taskId,
            List<ShipmentRequest> requests
    ) {
        taskRegistry.markRunning(taskId);
        try {
            Thread.sleep(SIMULATED_DELAY_MILLIS);
            List<ShipmentResponse> createdShipments = shipmentService.createBulk(requests);
            taskRegistry.markCompleted(taskId, createdShipments);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            taskRegistry.markFailed(taskId, "Async task was interrupted");
        } catch (Exception exception) {
            taskRegistry.markFailed(taskId, resolveErrorMessage(exception));
        }
        return CompletableFuture.completedFuture(null);
    }

    private String resolveErrorMessage(Exception exception) {
        if (exception instanceof ResponseStatusException responseStatusException
                && responseStatusException.getReason() != null) {
            return responseStatusException.getReason();
        }
        if (exception.getMessage() == null || exception.getMessage().isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return exception.getMessage();
    }
}
