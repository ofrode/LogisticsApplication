package com.logisticsapplication.service;

import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.response.PageResponse;
import com.logisticsapplication.dto.response.ShipmentResponse;
import com.logisticsapplication.model.ShipmentSearchQueryType;
import com.logisticsapplication.model.ShipmentStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ShipmentService {

    ShipmentResponse create(ShipmentRequest request);

    ShipmentResponse update(Long id, ShipmentRequest request);

    ShipmentResponse getById(Long id);

    List<ShipmentResponse> getAll(ShipmentStatus status, boolean optimized);

    PageResponse<ShipmentResponse> search(
            String customerEmail,
            String cargoName,
            LocalDateTime arrivalFrom,
            LocalDateTime arrivalTo,
            ShipmentSearchQueryType queryType,
            Pageable pageable
    );

    void delete(Long id);

    ShipmentResponse createWithPartialSaveDemo(ShipmentRequest request);

    ShipmentResponse createWithRollbackDemo(ShipmentRequest request);
}
