package com.logisticsapplication.controller;

import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.response.ShipmentResponse;
import com.logisticsapplication.service.ShipmentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

    @PutMapping("/{id}")
    public ResponseEntity<ShipmentResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody ShipmentRequest request
    ) {
        return ResponseEntity.ok(shipmentService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(shipmentService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentResponse>> getAll(
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(shipmentService.getAll(status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        shipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
