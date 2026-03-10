package com.logisticsapplication.mapper;

import com.logisticsapplication.dto.response.VehicleResponse;
import com.logisticsapplication.model.Vehicle;

public final class VehicleMapper {

    private VehicleMapper() {
    }

    public static VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getRegistrationNumber(),
                vehicle.getCapacityKg(),
                AppUserMapper.toResponse(vehicle.getAssignedCarrier())
        );
    }
}
