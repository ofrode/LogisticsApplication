package com.logisticsapplication.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logisticsapplication.dto.request.CargoRequest;
import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.request.ShipmentScheduleRequest;
import com.logisticsapplication.dto.response.PageResponse;
import com.logisticsapplication.dto.response.ShipmentResponse;
import com.logisticsapplication.model.ShipmentSearchQueryType;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ShipmentControllerTest {

    @Mock
    private ShipmentService shipmentService;

    private ShipmentController shipmentController;

    @BeforeEach
    void setUp() {
        shipmentController = new ShipmentController(shipmentService);
    }

    @Test
    void createReturnsCreatedShipment() {
        ShipmentRequest request = shipmentRequest("SHIP-100");
        ShipmentResponse response = shipmentResponse(1L, "SHIP-100");
        when(shipmentService.create(request)).thenReturn(response);

        assertThat(shipmentController.create(request).getBody()).isEqualTo(response);
        verify(shipmentService).create(request);
    }

    @Test
    void bulkAndDemoEndpointsDelegateToService() {
        ShipmentRequest firstRequest = shipmentRequest("SHIP-101");
        ShipmentRequest secondRequest = shipmentRequest("SHIP-102");
        List<ShipmentRequest> requests = List.of(firstRequest, secondRequest);
        List<ShipmentResponse> responses = List.of(
                shipmentResponse(1L, "SHIP-101"),
                shipmentResponse(2L, "SHIP-102")
        );

        when(shipmentService.createBulk(requests)).thenReturn(responses);
        when(shipmentService.createBulkWithPartialSaveDemo(requests)).thenReturn(responses);
        when(shipmentService.createBulkWithRollbackDemo(requests)).thenReturn(responses);
        when(shipmentService.createWithPartialSaveDemo(firstRequest)).thenReturn(responses.get(0));
        when(shipmentService.createWithRollbackDemo(firstRequest)).thenReturn(responses.get(0));

        assertThat(shipmentController.createBulk(requests).getBody()).isEqualTo(responses);
        assertThat(shipmentController.createBulkWithPartialSave(requests).getBody()).isEqualTo(responses);
        assertThat(shipmentController.createBulkWithRollback(requests).getBody()).isEqualTo(responses);
        assertThat(shipmentController.createWithPartialSave(firstRequest).getBody()).isEqualTo(responses.get(0));
        assertThat(shipmentController.createWithRollback(firstRequest).getBody()).isEqualTo(responses.get(0));
    }

    @Test
    void readUpdateSearchAndDeleteEndpointsDelegateToService() {
        ShipmentRequest request = shipmentRequest("SHIP-103");
        ShipmentResponse response = shipmentResponse(3L, "SHIP-103");
        Pageable pageable = PageRequest.of(0, 5);
        PageResponse<ShipmentResponse> pageResponse = new PageResponse<>(
                List.of(response),
                0,
                5,
                1,
                1,
                false,
                ShipmentSearchQueryType.JPQL.name()
        );

        when(shipmentService.update(3L, request)).thenReturn(response);
        when(shipmentService.getById(3L)).thenReturn(response);
        when(shipmentService.getAll(ShipmentStatus.CREATED, true)).thenReturn(List.of(response));
        when(shipmentService.search(
                "client@example.com",
                "Paper",
                LocalDateTime.of(2026, 4, 10, 10, 0),
                LocalDateTime.of(2026, 4, 20, 10, 0),
                ShipmentSearchQueryType.JPQL,
                pageable
        )).thenReturn(pageResponse);

        assertThat(shipmentController.update(3L, request).getBody()).isEqualTo(response);
        assertThat(shipmentController.getById(3L).getBody()).isEqualTo(response);
        assertThat(shipmentController.getAll(ShipmentStatus.CREATED, true).getBody())
                .containsExactly(response);
        assertThat(shipmentController.search(
                "client@example.com",
                "Paper",
                LocalDateTime.of(2026, 4, 10, 10, 0),
                LocalDateTime.of(2026, 4, 20, 10, 0),
                ShipmentSearchQueryType.JPQL,
                pageable
        ).getBody()).isEqualTo(pageResponse);

        assertThat(shipmentController.delete(3L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(shipmentService).delete(3L);
    }

    private ShipmentRequest shipmentRequest(String trackingNumber) {
        return new ShipmentRequest(
                trackingNumber,
                "Minsk",
                "Prague",
                ShipmentStatus.CREATED,
                1L,
                2L,
                List.of(10L),
                List.of(new CargoRequest("Paper", new BigDecimal("100.50"))),
                new ShipmentScheduleRequest(
                        LocalDateTime.of(2026, 4, 1, 10, 0),
                        LocalDateTime.of(2026, 4, 1, 12, 0),
                        LocalDateTime.of(2026, 4, 2, 15, 0)
                )
        );
    }

    private ShipmentResponse shipmentResponse(Long id, String trackingNumber) {
        return new ShipmentResponse(
                id,
                trackingNumber,
                "Minsk",
                "Prague",
                ShipmentStatus.CREATED,
                null,
                null,
                List.of(),
                null,
                List.of()
        );
    }
}
