package com.logisticsapplication.service;

import com.logisticsapplication.dto.request.CargoAdminRequest;
import com.logisticsapplication.dto.response.CargoAdminResponse;
import java.util.List;

public interface CargoAdminService {

    List<CargoAdminResponse> getAll();

    CargoAdminResponse update(Long id, CargoAdminRequest request);

    void delete(Long id);
}
