package com.logisticsapplication.service;

import com.logisticsapplication.dto.request.VehicleRequest;
import com.logisticsapplication.dto.response.VehicleResponse;
import java.util.List;

public interface VehicleService {

    VehicleResponse create(VehicleRequest request);

    VehicleResponse update(Long id, VehicleRequest request);

    VehicleResponse getById(Long id);

    List<VehicleResponse> getAll();

    void delete(Long id);
}
