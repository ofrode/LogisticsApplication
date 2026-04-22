package com.logisticsapplication.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.logisticsapplication.dto.request.CargoRequest;
import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.request.ShipmentScheduleRequest;
import com.logisticsapplication.dto.response.AsyncShipmentTaskStatusResponse;
import com.logisticsapplication.dto.response.ShipmentResponse;
import com.logisticsapplication.model.AsyncTaskStatus;
import com.logisticsapplication.model.ShipmentStatus;
import com.logisticsapplication.service.ShipmentService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ShipmentAsyncWorkerTest {

    @Mock
    private ShipmentService shipmentService;

    private AsyncShipmentTaskRegistry taskRegistry;
    private ShipmentAsyncWorker shipmentAsyncWorker;

    @BeforeEach
    void setUp() {
        taskRegistry = new AsyncShipmentTaskRegistry();
        shipmentAsyncWorker = new ShipmentAsyncWorker(shipmentService, taskRegistry);
    }

    @Test
    void processBulkCreationMarksTaskCompleted() {
        List<ShipmentRequest> requests = List.of(buildRequest("ASYNC-001"));
        Long taskId = taskRegistry.createTask(requests).getTaskId();
        when(shipmentService.createBulk(requests)).thenReturn(
                List.of(new ShipmentResponse(
                        101L,
                        "ASYNC-001",
                        "Minsk",
                        "Prague",
                        ShipmentStatus.CREATED,
                        null,
                        null,
                        List.of(),
                        null,
                        List.of()
                ))
        );

        shipmentAsyncWorker.processBulkCreation(taskId, requests).join();

        AsyncShipmentTaskStatusResponse status = taskRegistry.getTask(taskId);
        assertThat(status.getStatus()).isEqualTo(AsyncTaskStatus.COMPLETED);
        assertThat(status.getProcessedShipments()).isEqualTo(1);
        assertThat(status.getSubmittedRequests()).containsExactlyElementsOf(requests);
        assertThat(status.getCreatedShipmentIds()).containsExactly(101L);
        assertThat(status.getCompletedAt()).isNotNull();
    }

    @Test
    void processBulkCreationMarksTaskFailedWhenBusinessOperationFails() {
        List<ShipmentRequest> requests = List.of(buildRequest("ASYNC-FAIL"));
        Long taskId = taskRegistry.createTask(requests).getTaskId();
        when(shipmentService.createBulk(requests)).thenThrow(
                new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Tracking number already exists: ASYNC-FAIL"
                )
        );

        shipmentAsyncWorker.processBulkCreation(taskId, requests).join();

        AsyncShipmentTaskStatusResponse status = taskRegistry.getTask(taskId);
        assertThat(status.getStatus()).isEqualTo(AsyncTaskStatus.FAILED);
        assertThat(status.getSubmittedRequests()).containsExactlyElementsOf(requests);
        assertThat(status.getErrorMessage()).isEqualTo(
                "Tracking number already exists: ASYNC-FAIL"
        );
        assertThat(status.getCompletedAt()).isNotNull();
    }

    @Test
    void getAllTasksReturnsTasksInReverseCreationOrder() {
        Long firstTaskId = taskRegistry.createTask(List.of(buildRequest("ASYNC-FIRST"))).getTaskId();
        Long secondTaskId = taskRegistry.createTask(List.of(buildRequest("ASYNC-SECOND"))).getTaskId();

        var overview = taskRegistry.getAllTasks();

        assertThat(overview.getSubmittedTasks())
                .extracting(AsyncShipmentTaskStatusResponse::getTaskId)
                .containsExactly(secondTaskId, firstTaskId);
    }

    private ShipmentRequest buildRequest(String trackingNumber) {
        return new ShipmentRequest(
                trackingNumber,
                "Minsk",
                "Prague",
                ShipmentStatus.CREATED,
                1L,
                2L,
                List.of(10L),
                List.of(new CargoRequest("Paper", new BigDecimal("10.00"))),
                new ShipmentScheduleRequest(
                        LocalDateTime.of(2026, 4, 14, 10, 0),
                        LocalDateTime.of(2026, 4, 14, 12, 0),
                        LocalDateTime.of(2026, 4, 15, 12, 0)
                )
        );
    }
}
