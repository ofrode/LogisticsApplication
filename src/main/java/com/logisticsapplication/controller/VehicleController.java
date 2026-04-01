package com.logisticsapplication.controller;

import com.logisticsapplication.dto.request.VehicleRequest;
import com.logisticsapplication.dto.response.ApiErrorResponse;
import com.logisticsapplication.dto.response.VehicleResponse;
import com.logisticsapplication.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "Vehicles", description = "Операции с транспортом")
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @Operation(summary = "Создать транспорт")
    @ApiResponse(responseCode = "200", description = "Транспорт создан")
    @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.ok(vehicleService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить транспорт")
    public ResponseEntity<VehicleResponse> update(
            @PathVariable @Positive(message = "id must be positive") Long id,
            @Valid @RequestBody VehicleRequest request
    ) {
        return ResponseEntity.ok(vehicleService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить транспорт по id")
    public ResponseEntity<VehicleResponse> getById(
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        return ResponseEntity.ok(vehicleService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Получить список транспорта")
    public ResponseEntity<List<VehicleResponse>> getAll() {
        return ResponseEntity.ok(vehicleService.getAll());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить транспорт")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
