package com.logisticsapplication.controller;

import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.response.PageResponse;
import com.logisticsapplication.dto.response.ShipmentResponse;
import com.logisticsapplication.model.ShipmentSearchQueryType;
import com.logisticsapplication.model.ShipmentStatus;
import com.logisticsapplication.service.ShipmentService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody ShipmentRequest request) {
        return ResponseEntity.ok(shipmentService.create(request));
    }

    @PostMapping("/demo/partial-save")
    public ResponseEntity<ShipmentResponse> createWithPartialSave(
            @Valid @RequestBody ShipmentRequest request
    ) {
        return ResponseEntity.ok(shipmentService.createWithPartialSaveDemo(request));
    }

    @PostMapping("/demo/rollback")
    public ResponseEntity<ShipmentResponse> createWithRollback(
            @Valid @RequestBody ShipmentRequest request
    ) {
        return ResponseEntity.ok(shipmentService.createWithRollbackDemo(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShipmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ShipmentRequest request
    ) {
        return ResponseEntity.ok(shipmentService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentResponse>> getAll(
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(defaultValue = "false") boolean optimized
    ) {
        return ResponseEntity.ok(shipmentService.getAll(status, optimized));
    }

    @GetMapping("/search")
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
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
