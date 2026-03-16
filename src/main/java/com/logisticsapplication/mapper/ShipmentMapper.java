package com.logisticsapplication.mapper;

import com.logisticsapplication.dto.response.CargoResponse;
import com.logisticsapplication.dto.response.ShipmentResponse;
import com.logisticsapplication.dto.response.ShipmentScheduleResponse;
import com.logisticsapplication.model.Cargo;
import com.logisticsapplication.model.Shipment;
import com.logisticsapplication.model.ShipmentSchedule;
import com.logisticsapplication.model.ShipmentStatus;

public final class ShipmentMapper {

    private ShipmentMapper() {
    }

    public static ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getTrackingNumber(),
                shipment.getOriginCity(),
                shipment.getDestinationCity(),
                ShipmentStatus.valueOf(shipment.getStatus().getCode()),
                AppUserMapper.toResponse(shipment.getCustomer()),
                AppUserMapper.toResponse(shipment.getManager()),
                shipment.getCargoes().stream()
                        .map(ShipmentMapper::toCargoResponse)
                        .toList(),
                toScheduleResponse(shipment.getSchedule()),
                shipment.getVehicles().stream()
                        .map(VehicleMapper::toResponse)
                        .toList()
        );
    }

    private static CargoResponse toCargoResponse(Cargo cargo) {
        return new CargoResponse(cargo.getId(), cargo.getName(), cargo.getWeightKg());
    }

    private static ShipmentScheduleResponse toScheduleResponse(ShipmentSchedule schedule) {
        if (schedule == null) {
            return null;
        }
        return new ShipmentScheduleResponse(
                schedule.getId(),
                schedule.getOrderCreatedAt(),
                schedule.getOrderReceivedAt(),
                schedule.getArrivalAt()
        );
    }
}
