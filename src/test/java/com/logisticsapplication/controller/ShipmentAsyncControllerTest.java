package com.logisticsapplication.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logisticsapplication.dto.request.CargoRequest;
import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.request.ShipmentScheduleRequest;
import com.logisticsapplication.dto.response.AsyncShipmentTaskStatusResponse;
import com.logisticsapplication.model.AsyncTaskStatus;
import com.logisticsapplication.model.ShipmentStatus;
import com.logisticsapplication.service.ShipmentAsyncService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShipmentAsyncControllerTest {

    @Mock
    private ShipmentAsyncService shipmentAsyncService;

    private ShipmentAsyncController shipmentAsyncController;

    @BeforeEach
    void setUp() {
        shipmentAsyncController = new ShipmentAsyncController(shipmentAsyncService);
    }

    @Test
    void submitBulkCreateTaskReturnsTaskId() {
        List<ShipmentRequest> requests = List.of(buildRequest("ASYNC-001"));
        when(shipmentAsyncService.submitBulkCreateTask(requests)).thenReturn(15L);

        var response = shipmentAsyncController.submitBulkCreateTask(requests).getBody();

        assertThat(response.getTaskId()).isEqualTo(15L);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        verify(shipmentAsyncService).submitBulkCreateTask(requests);
    }

    @Test
    void getTaskStatusDelegatesToService() {
        AsyncShipmentTaskStatusResponse response = new AsyncShipmentTaskStatusResponse(
                15L,
                AsyncTaskStatus.COMPLETED,
                2,
                2,
                List.of(101L, 102L),
                null,
                Instant.parse("2026-04-14T11:00:00Z"),
                Instant.parse("2026-04-14T11:00:01Z"),
                Instant.parse("2026-04-14T11:00:03Z")
        );
        when(shipmentAsyncService.getTaskStatus(15L)).thenReturn(response);

        assertThat(shipmentAsyncController.getTaskStatus(15L).getBody()).isEqualTo(response);
        verify(shipmentAsyncService).getTaskStatus(15L);
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
