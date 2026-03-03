package com.logisticsapplication.service;

import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.response.ShipmentResponse;
import java.util.List;

public interface ShipmentService {

    ShipmentResponse create(ShipmentRequest request);

    ShipmentResponse update(Integer id, ShipmentRequest request);

    ShipmentResponse getById(Integer id);

    List<ShipmentResponse> getAll(String status);

    void delete(Integer id);
}
