package com.logisticsapplication.controller;

import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.response.ApiErrorResponse;
import com.logisticsapplication.dto.response.AsyncShipmentTaskStatusResponse;
import com.logisticsapplication.dto.response.AsyncTaskSubmittedResponse;
import com.logisticsapplication.service.ShipmentAsyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Tag(name = "Shipment Async", description = "Асинхронные операции с отправлениями")
@RequestMapping("/api/shipments/async")
public class ShipmentAsyncController {

    private final ShipmentAsyncService shipmentAsyncService;

    public ShipmentAsyncController(ShipmentAsyncService shipmentAsyncService) {
        this.shipmentAsyncService = shipmentAsyncService;
    }

    @PostMapping("/bulk")
    @Operation(summary = "Запустить асинхронное bulk-создание отправлений")
    @ApiResponse(responseCode = "200", description = "Задача поставлена в очередь")
    @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public ResponseEntity<AsyncTaskSubmittedResponse> submitBulkCreateTask(
            @RequestBody @NotEmpty List<@Valid ShipmentRequest> requests
    ) {
        Long taskId = shipmentAsyncService.submitBulkCreateTask(requests);
        return ResponseEntity.ok(
                new AsyncTaskSubmittedResponse(taskId, "PENDING")
        );
    }

    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "Получить статус асинхронной задачи")
    @ApiResponse(responseCode = "200", description = "Статус получен")
    @ApiResponse(
            responseCode = "404",
            description = "Задача не найдена",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public ResponseEntity<AsyncShipmentTaskStatusResponse> getTaskStatus(
            @PathVariable @Positive(message = "taskId must be positive") Long taskId
    ) {
        return ResponseEntity.ok(shipmentAsyncService.getTaskStatus(taskId));
    }
}
