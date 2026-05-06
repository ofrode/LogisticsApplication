package com.logisticsapplication.service.impl;

import com.logisticsapplication.cache.ShipmentSearchIndex;
import com.logisticsapplication.dto.request.CargoAdminRequest;
import com.logisticsapplication.dto.response.CargoAdminResponse;
import com.logisticsapplication.model.Cargo;
import com.logisticsapplication.model.Shipment;
import com.logisticsapplication.repository.CargoRepository;
import com.logisticsapplication.repository.ShipmentRepository;
import com.logisticsapplication.service.CargoAdminService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CargoAdminServiceImpl implements CargoAdminService {

    private final CargoRepository cargoRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentSearchIndex shipmentSearchIndex;

    @Override
    public List<CargoAdminResponse> getAll() {
        return cargoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CargoAdminResponse update(Long id, CargoAdminRequest request) {
        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Cargo not found: " + id
                        )
                );
        Shipment shipment = shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found: " + request.getShipmentId()
                ));
        cargo.setName(request.getName());
        cargo.setWeightKg(request.getWeightKg());
        cargo.setShipment(shipment);
        CargoAdminResponse response = toResponse(cargoRepository.save(cargo));
        shipmentSearchIndex.invalidateAll();
        return response;
    }

    @Override
    public void delete(Long id) {
        if (!cargoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cargo not found: " + id);
        }
        cargoRepository.deleteById(id);
        shipmentSearchIndex.invalidateAll();
    }

    private CargoAdminResponse toResponse(Cargo cargo) {
        return new CargoAdminResponse(
                cargo.getId(),
                cargo.getName(),
                cargo.getWeightKg(),
                cargo.getShipment().getId(),
                cargo.getShipment().getTrackingNumber()
        );
    }
}
