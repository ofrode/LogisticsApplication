package com.logisticsapplication.controller;

import com.logisticsapplication.dto.request.CargoAdminRequest;
import com.logisticsapplication.dto.response.CargoAdminResponse;
import com.logisticsapplication.service.CargoAdminService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/cargoes")
@Tag(name = "Cargo Admin", description = "Операции администратора с грузами")
public class CargoAdminController {

    private final CargoAdminService cargoAdminService;

    @GetMapping
    @Operation(summary = "Получить список грузов")
    public ResponseEntity<List<CargoAdminResponse>> getAll() {
        return ResponseEntity.ok(cargoAdminService.getAll());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить груз")
    public ResponseEntity<CargoAdminResponse> update(
            @PathVariable @Positive(message = "id must be positive") Long id,
            @Valid @RequestBody CargoAdminRequest request
    ) {
        return ResponseEntity.ok(cargoAdminService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить груз")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        cargoAdminService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
