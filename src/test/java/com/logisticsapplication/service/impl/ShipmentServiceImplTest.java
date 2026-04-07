package com.logisticsapplication.service.impl;

import com.logisticsapplication.cache.ShipmentSearchIndex;
import com.logisticsapplication.dto.request.CargoRequest;
import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.request.ShipmentScheduleRequest;
import com.logisticsapplication.dto.response.ShipmentResponse;
import com.logisticsapplication.model.AppUser;
import com.logisticsapplication.model.Shipment;
import com.logisticsapplication.model.ShipmentStatus;
import com.logisticsapplication.model.ShipmentStatusLookup;
import com.logisticsapplication.model.UserRole;
import com.logisticsapplication.model.UserRoleLookup;
import com.logisticsapplication.model.Vehicle;
import com.logisticsapplication.repository.AppUserRepository;
import com.logisticsapplication.repository.CargoRepository;
import com.logisticsapplication.repository.ShipmentRepository;
import com.logisticsapplication.repository.ShipmentScheduleRepository;
import com.logisticsapplication.repository.ShipmentStatusLookupRepository;
import com.logisticsapplication.repository.VehicleRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceImplTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ShipmentScheduleRepository shipmentScheduleRepository;

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private ShipmentStatusLookupRepository shipmentStatusLookupRepository;

    @Mock
    private ShipmentSearchIndex shipmentSearchIndex;

    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    private AppUser customer;
    private AppUser manager;
    private Vehicle vehicle;
    private ShipmentStatusLookup createdStatus;
    private ShipmentStatusLookup receivedStatus;

    @BeforeEach
    void setUp() {
        UserRoleLookup customerRole = new UserRoleLookup(1L, UserRole.CUSTOMER.name());
        UserRoleLookup managerRole = new UserRoleLookup(2L, UserRole.MANAGER.name());
        UserRoleLookup carrierRole = new UserRoleLookup(3L, UserRole.CARRIER.name());

        customer = new AppUser(10L, "Ivan", "Customer", "customer@test.local", customerRole,
                List.of(), List.of(), List.of());
        manager = new AppUser(11L, "Petr", "Manager", "manager@test.local", managerRole,
                List.of(), List.of(), List.of());
        AppUser carrier = new AppUser(12L, "Oleg", "Carrier", "carrier@test.local", carrierRole,
                List.of(), List.of(), List.of());

        vehicle = new Vehicle(20L, "TRUCK-20", new BigDecimal("4500.00"), carrier, Set.of());
        createdStatus = new ShipmentStatusLookup(100L, ShipmentStatus.CREATED.name());
        receivedStatus = new ShipmentStatusLookup(101L, ShipmentStatus.RECEIVED.name());
    }

    @Test
    void createBulkCreatesAllShipmentsAndInvalidatesCache() {
        stubSuccessfulBulkCreation();

        List<ShipmentResponse> responses = shipmentService.createBulk(List.of(
                buildRequest("BULK-001", ShipmentStatus.CREATED),
                buildRequest("BULK-002", ShipmentStatus.RECEIVED)
        ));

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ShipmentResponse::getTrackingNumber)
                .containsExactly("BULK-001", "BULK-002");
        assertThat(responses).extracting(ShipmentResponse::getStatus)
                .containsExactly(ShipmentStatus.CREATED, ShipmentStatus.RECEIVED);
        verify(shipmentRepository, times(2)).save(any(Shipment.class));
        verify(shipmentSearchIndex).invalidateAll();
    }

    @Test
    void createBulkRejectsDuplicateTrackingNumbersInsideRequestList() {
        List<ShipmentRequest> requests = List.of(
                buildRequest("DUPLICATE-001", ShipmentStatus.CREATED),
                buildRequest("DUPLICATE-001", ShipmentStatus.RECEIVED)
        );

        assertThatThrownBy(() -> shipmentService.createBulk(requests))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("duplicate tracking numbers");

        verify(shipmentRepository, never()).findByTrackingNumber(anyString());
        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    @Test
    void createBulkRejectsTrackingNumberThatAlreadyExistsInDatabase() {
        Shipment existingShipment = new Shipment();
        existingShipment.setId(77L);
        List<ShipmentRequest> requests = List.of(
                buildRequest("BULK-EXISTS", ShipmentStatus.CREATED)
        );
        when(shipmentRepository.findByTrackingNumber("BULK-EXISTS"))
                .thenReturn(Optional.of(existingShipment));

        assertThatThrownBy(() -> shipmentService.createBulk(requests))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tracking number already exists");

        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    private ShipmentRequest buildRequest(String trackingNumber, ShipmentStatus status) {
        return new ShipmentRequest(
                trackingNumber,
                "Minsk",
                "Warsaw",
                status,
                customer.getId(),
                manager.getId(),
                List.of(vehicle.getId()),
                List.of(new CargoRequest("Boxes", new BigDecimal("150.00"))),
                new ShipmentScheduleRequest(
                        LocalDateTime.of(2026, 4, 1, 10, 0),
                        LocalDateTime.of(2026, 4, 1, 12, 0),
                        LocalDateTime.of(2026, 4, 3, 18, 0)
                )
        );
    }

    private void stubSuccessfulBulkCreation() {
        when(appUserRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(appUserRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
        when(vehicleRepository.findAllById(List.of(vehicle.getId()))).thenReturn(List.of(vehicle));
        when(shipmentStatusLookupRepository.findByCode(ShipmentStatus.CREATED.name()))
                .thenReturn(Optional.of(createdStatus));
        when(shipmentStatusLookupRepository.findByCode(ShipmentStatus.RECEIVED.name()))
                .thenReturn(Optional.of(receivedStatus));
        when(shipmentRepository.findByTrackingNumber(anyString())).thenReturn(Optional.empty());

        AtomicLong sequence = new AtomicLong(1L);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
            Shipment shipment = invocation.getArgument(0);
            shipment.setId(sequence.getAndIncrement());
            return shipment;
        });
    }
}
