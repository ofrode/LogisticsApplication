package com.logisticsapplication.mapper;

import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.response.ShipmentResponse;
import com.logisticsapplication.model.Shipment;

public final class ShipmentMapper {

    private ShipmentMapper() {
    }

    public static ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getCargoName(),
                shipment.getOriginCity(),
                shipment.getDestinationCity(),
                shipment.getPickupDate(),
                shipment.getWeightKg(),
                shipment.getStatus()
        );
    }

    public static void updateEntity(Shipment shipment, ShipmentRequest request) {
        shipment.setCargoName(request.getCargoName());
        shipment.setOriginCity(request.getOriginCity());
        shipment.setDestinationCity(request.getDestinationCity());
        shipment.setPickupDate(request.getPickupDate());
        shipment.setWeightKg(request.getWeightKg());
        shipment.setStatus(request.getStatus());
    }
}
