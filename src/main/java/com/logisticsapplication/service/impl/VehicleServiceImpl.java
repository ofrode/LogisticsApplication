package com.logisticsapplication.service.impl;

import com.logisticsapplication.cache.ShipmentSearchIndex;
import com.logisticsapplication.dto.request.VehicleRequest;
import com.logisticsapplication.dto.response.VehicleResponse;
import com.logisticsapplication.mapper.VehicleMapper;
import com.logisticsapplication.model.AppUser;
import com.logisticsapplication.model.UserRole;
import com.logisticsapplication.model.Vehicle;
import com.logisticsapplication.repository.AppUserRepository;
import com.logisticsapplication.repository.VehicleRepository;
import com.logisticsapplication.service.VehicleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final AppUserRepository appUserRepository;
    private final ShipmentSearchIndex shipmentSearchIndex;

    @Override
    public VehicleResponse create(VehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        apply(vehicle, request);
        VehicleResponse response = VehicleMapper.toResponse(vehicleRepository.save(vehicle));
        shipmentSearchIndex.invalidateAll();
        return response;
    }

    @Override
    public VehicleResponse update(Long id, VehicleRequest request) {
        Vehicle vehicle = getEntity(id);
        apply(vehicle, request);
        VehicleResponse response = VehicleMapper.toResponse(vehicleRepository.save(vehicle));
        shipmentSearchIndex.invalidateAll();
        return response;
    }

    @Override
    public VehicleResponse getById(Long id) {
        return VehicleMapper.toResponse(getEntity(id));
    }

    @Override
    public List<VehicleResponse> getAll() {
        return vehicleRepository.findAll().stream()
                .map(VehicleMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found: " + id);
        }
        vehicleRepository.deleteById(id);
        shipmentSearchIndex.invalidateAll();
    }

    private void apply(Vehicle vehicle, VehicleRequest request) {
        AppUser carrier = appUserRepository.findById(request.getCarrierId()).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Carrier not found: " + request.getCarrierId()
                )
        );
        if (!UserRole.CARRIER.name().equals(carrier.getRole().getCode())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Assigned user must have role CARRIER"
            );
        }
        vehicle.setRegistrationNumber(request.getRegistrationNumber());
        vehicle.setCapacityKg(request.getCapacityKg());
        vehicle.setAssignedCarrier(carrier);
    }

    private Vehicle getEntity(Long id) {
        return vehicleRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found: " + id)
        );
    }
}
