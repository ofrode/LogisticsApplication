package com.logisticsapplication.service;

import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.response.ShipmentResponse;
import com.logisticsapplication.model.ShipmentStatus;
import java.util.List;

public interface ShipmentService {

    ShipmentResponse create(ShipmentRequest request);

    ShipmentResponse update(Long id, ShipmentRequest request);

    ShipmentResponse getById(Long id);

    List<ShipmentResponse> getAll(ShipmentStatus status, boolean optimized);

    void delete(Long id);

    ShipmentResponse createWithPartialSaveDemo(ShipmentRequest request);

    ShipmentResponse createWithRollbackDemo(ShipmentRequest request);
}
