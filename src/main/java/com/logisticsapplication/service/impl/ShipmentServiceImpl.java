package com.logisticsapplication.service.impl;

import com.logisticsapplication.dto.request.CargoRequest;
import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.request.ShipmentScheduleRequest;
import com.logisticsapplication.dto.response.ShipmentResponse;
import com.logisticsapplication.mapper.ShipmentMapper;
import com.logisticsapplication.model.AppUser;
import com.logisticsapplication.model.Cargo;
import com.logisticsapplication.model.Shipment;
import com.logisticsapplication.model.ShipmentSchedule;
import com.logisticsapplication.model.ShipmentStatus;
import com.logisticsapplication.model.UserRole;
import com.logisticsapplication.model.Vehicle;
import com.logisticsapplication.repository.AppUserRepository;
import com.logisticsapplication.repository.CargoRepository;
import com.logisticsapplication.repository.ShipmentRepository;
import com.logisticsapplication.repository.ShipmentScheduleRepository;
import com.logisticsapplication.repository.VehicleRepository;
import com.logisticsapplication.service.ShipmentService;
import jakarta.transaction.Transactional;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final AppUserRepository appUserRepository;
    private final VehicleRepository vehicleRepository;
    private final ShipmentScheduleRepository shipmentScheduleRepository;
    private final CargoRepository cargoRepository;

    @Override
    @Transactional
    public ShipmentResponse create(ShipmentRequest request) {
        Shipment shipment = new Shipment();
        applyAggregate(shipment, request);
        return ShipmentMapper.toResponse(shipmentRepository.save(shipment));
    }

    @Override
    @Transactional
    public ShipmentResponse update(Long id, ShipmentRequest request) {
        Shipment shipment = shipmentRepository.findDetailedById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found: " + id)
        );
        applyAggregate(shipment, request);
        return ShipmentMapper.toResponse(shipmentRepository.save(shipment));
    }

    @Override
    @Transactional
    public ShipmentResponse getById(Long id) {
        Shipment shipment = shipmentRepository.findDetailedById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found: " + id)
        );
        return ShipmentMapper.toResponse(shipment);
    }

    @Override
    @Transactional
    public List<ShipmentResponse> getAll(ShipmentStatus status, boolean optimized) {
        List<Shipment> shipments;
        if (optimized) {
            shipments = status == null
                    ? shipmentRepository.findAllWithDetails()
                    : shipmentRepository.findByStatusOrderByIdAsc(status);
        } else {
            shipments = status == null
                    ? shipmentRepository.findAll()
                    : shipmentRepository.findByStatus(status);
        }
        return shipments.stream()
                .map(ShipmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!shipmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found: " + id);
        }
        shipmentRepository.deleteById(id);
    }

    @Override
    public ShipmentResponse createWithPartialSaveDemo(ShipmentRequest request) {
        return saveWithManualSteps(request, true);
    }

    @Override
    @Transactional
    public ShipmentResponse createWithRollbackDemo(ShipmentRequest request) {
        return saveWithManualSteps(request, true);
    }

    private ShipmentResponse saveWithManualSteps(
            ShipmentRequest request,
            boolean failAfterFirstCargo
    ) {
        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(request.getTrackingNumber());
        shipment.setOriginCity(request.getOriginCity());
        shipment.setDestinationCity(request.getDestinationCity());
        shipment.setStatus(request.getStatus());
        shipment.setCustomer(
                getUserByRole(request.getCustomerId(), UserRole.CUSTOMER, "Customer")
        );
        shipment.setManager(
                getUserByRole(request.getManagerId(), UserRole.MANAGER, "Manager")
        );
        shipment.setVehicles(resolveVehicles(request.getVehicleIds()));
        Shipment persistedShipment = shipmentRepository.save(shipment);

        ShipmentSchedule schedule = buildSchedule(request.getSchedule());
        schedule.setShipment(persistedShipment);
        shipmentScheduleRepository.save(schedule);
        persistedShipment.setSchedule(schedule);

        CargoRequest firstCargoRequest = request.getCargoes().getFirst();
        Cargo firstCargo = new Cargo(
                null,
                firstCargoRequest.getName(),
                firstCargoRequest.getWeightKg(),
                persistedShipment
        );
        cargoRepository.save(firstCargo);
        persistedShipment.getCargoes().add(firstCargo);

        if (failAfterFirstCargo) {
            throw new IllegalStateException("Intentional failure after partial save");
        }

        for (int index = 1; index < request.getCargoes().size(); index++) {
            CargoRequest cargoRequest = request.getCargoes().get(index);
            Cargo cargo = new Cargo(
                    null,
                    cargoRequest.getName(),
                    cargoRequest.getWeightKg(),
                    persistedShipment
            );
            cargoRepository.save(cargo);
            persistedShipment.getCargoes().add(cargo);
        }
        return ShipmentMapper.toResponse(persistedShipment);
    }

    private void applyAggregate(Shipment shipment, ShipmentRequest request) {
        shipment.setTrackingNumber(request.getTrackingNumber());
        shipment.setOriginCity(request.getOriginCity());
        shipment.setDestinationCity(request.getDestinationCity());
        shipment.setStatus(request.getStatus());
        shipment.setCustomer(
                getUserByRole(request.getCustomerId(), UserRole.CUSTOMER, "Customer")
        );
        shipment.setManager(
                getUserByRole(request.getManagerId(), UserRole.MANAGER, "Manager")
        );
        shipment.setVehicles(resolveVehicles(request.getVehicleIds()));
        shipment.clearCargoes();
        request.getCargoes().forEach(
                cargoRequest -> shipment.addCargo(buildCargo(cargoRequest))
        );
        shipment.setSchedule(buildSchedule(request.getSchedule()));
    }

    private Cargo buildCargo(CargoRequest request) {
        return new Cargo(null, request.getName(), request.getWeightKg(), null);
    }

    private ShipmentSchedule buildSchedule(ShipmentScheduleRequest request) {
        return new ShipmentSchedule(
                null,
                request.getOrderCreatedAt(),
                request.getOrderReceivedAt(),
                request.getArrivalAt(),
                null
        );
    }

    private Set<Vehicle> resolveVehicles(List<Long> vehicleIds) {
        Set<Vehicle> vehicles = new LinkedHashSet<>(vehicleRepository.findAllById(vehicleIds));
        if (vehicles.size() != vehicleIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Some vehicles were not found"
            );
        }
        return vehicles;
    }

    private AppUser getUserByRole(Long id, UserRole role, String label) {
        AppUser user = appUserRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, label + " not found: " + id)
        );
        if (user.getRole() != role) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    label + " must have role " + role
            );
        }
        return user;
    }
}
