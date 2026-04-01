package com.logisticsapplication.controller;

import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.response.ApiErrorResponse;
import com.logisticsapplication.dto.response.PageResponse;
import com.logisticsapplication.dto.response.ShipmentResponse;
import com.logisticsapplication.model.ShipmentSearchQueryType;
import com.logisticsapplication.model.ShipmentStatus;
import com.logisticsapplication.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "Shipments", description = "Операции с отправлениями")
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    @Operation(summary = "Создать отправление")
    @ApiResponse(responseCode = "200", description = "Отправление создано")
    @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody ShipmentRequest request) {
        return ResponseEntity.ok(shipmentService.create(request));
    }

    @PostMapping("/demo/partial-save")
    @Operation(summary = "Демо частичного сохранения без транзакционного отката")
    public ResponseEntity<ShipmentResponse> createWithPartialSave(
            @Valid @RequestBody ShipmentRequest request
    ) {
        return ResponseEntity.ok(shipmentService.createWithPartialSaveDemo(request));
    }

    @PostMapping("/demo/rollback")
    @Operation(summary = "Демо отката в транзакции")
    public ResponseEntity<ShipmentResponse> createWithRollback(
            @Valid @RequestBody ShipmentRequest request
    ) {
        return ResponseEntity.ok(shipmentService.createWithRollbackDemo(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить отправление")
    public ResponseEntity<ShipmentResponse> update(
            @PathVariable @Positive(message = "id must be positive") Long id,
            @Valid @RequestBody ShipmentRequest request
    ) {
        return ResponseEntity.ok(shipmentService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить отправление по id")
    public ResponseEntity<ShipmentResponse> getById(
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        return ResponseEntity.ok(shipmentService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Получить список отправлений")
    public ResponseEntity<List<ShipmentResponse>> getAll(
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(defaultValue = "false") boolean optimized
    ) {
        return ResponseEntity.ok(shipmentService.getAll(status, optimized));
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск отправлений с пагинацией")
    public ResponseEntity<PageResponse<ShipmentResponse>> search(
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) String cargoName,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime arrivalFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime arrivalTo,
            @RequestParam(defaultValue = "JPQL") ShipmentSearchQueryType queryType,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                shipmentService.search(
                        customerEmail,
                        cargoName,
                        arrivalFrom,
                        arrivalTo,
                        queryType,
                        pageable
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить отправление")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        shipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
