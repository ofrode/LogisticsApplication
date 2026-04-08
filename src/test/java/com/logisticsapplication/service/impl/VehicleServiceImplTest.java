package com.logisticsapplication.service.impl;

import com.logisticsapplication.cache.ShipmentSearchIndex;
import com.logisticsapplication.dto.request.VehicleRequest;
import com.logisticsapplication.dto.response.VehicleResponse;
import com.logisticsapplication.model.AppUser;
import com.logisticsapplication.model.UserRole;
import com.logisticsapplication.model.UserRoleLookup;
import com.logisticsapplication.model.Vehicle;
import com.logisticsapplication.repository.AppUserRepository;
import com.logisticsapplication.repository.VehicleRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private ShipmentSearchIndex shipmentSearchIndex;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    @Test
    void createAssignsCarrierAndInvalidatesCache() {
        AppUser carrier = buildUser(3L, UserRole.CARRIER);
        when(appUserRepository.findById(3L)).thenReturn(Optional.of(carrier));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle vehicle = invocation.getArgument(0);
            vehicle.setId(8L);
            return vehicle;
        });

        VehicleResponse response = vehicleService.create(request("TRUCK-8080", "7000.00", 3L));

        assertThat(response.getId()).isEqualTo(8L);
        assertThat(response.getRegistrationNumber()).isEqualTo("TRUCK-8080");
        assertThat(response.getCarrier().getRole()).isEqualTo(UserRole.CARRIER);
        verify(shipmentSearchIndex).invalidateAll();
    }

    @Test
    void createRejectsMissingCarrier() {
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.create(request("TRUCK-404", "5000.00", 99L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Carrier not found: 99");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    @Test
    void createRejectsAssignedUserWithoutCarrierRole() {
        when(appUserRepository.findById(4L)).thenReturn(Optional.of(buildUser(4L, UserRole.CUSTOMER)));

        assertThatThrownBy(() -> vehicleService.create(request("TRUCK-FAIL", "5000.00", 4L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Assigned user must have role CARRIER");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    @Test
    void updateExistingVehicleChangesAssignedCarrierAndInvalidatesCache() {
        AppUser oldCarrier = buildUser(10L, UserRole.CARRIER);
        AppUser newCarrier = buildUser(11L, UserRole.CARRIER);
        Vehicle existingVehicle = new Vehicle(
                15L,
                "OLD-REG",
                new BigDecimal("4500.00"),
                oldCarrier,
                Set.of()
        );
        when(vehicleRepository.findDetailedById(15L)).thenReturn(Optional.of(existingVehicle));
        when(appUserRepository.findById(11L)).thenReturn(Optional.of(newCarrier));
        when(vehicleRepository.save(existingVehicle)).thenReturn(existingVehicle);

        VehicleResponse response = vehicleService.update(15L, request("NEW-REG", "9000.00", 11L));

        assertThat(response.getId()).isEqualTo(15L);
        assertThat(response.getRegistrationNumber()).isEqualTo("NEW-REG");
        assertThat(response.getCapacityKg()).isEqualByComparingTo("9000.00");
        assertThat(response.getCarrier().getEmail()).isEqualTo("carrier11@test.local");
        assertThat(existingVehicle.getAssignedCarrier()).isEqualTo(newCarrier);
        verify(shipmentSearchIndex).invalidateAll();
    }

    @Test
    void updateMissingVehicleThrowsNotFound() {
        when(vehicleRepository.findDetailedById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.update(42L, request("NEW-REG", "9000.00", 11L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Vehicle not found: 42");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    @Test
    void getByIdMapsVehicleWithCarrier() {
        when(vehicleRepository.findDetailedById(9L)).thenReturn(Optional.of(
                new Vehicle(
                        9L,
                        "TRUCK-9000",
                        new BigDecimal("6500.00"),
                        buildUser(7L, UserRole.CARRIER),
                        Set.of()
                )
        ));

        VehicleResponse response = vehicleService.getById(9L);

        assertThat(response.getId()).isEqualTo(9L);
        assertThat(response.getCarrier().getEmail()).isEqualTo("carrier7@test.local");
    }

    @Test
    void getByIdMissingVehicleThrowsNotFound() {
        when(vehicleRepository.findDetailedById(91L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getById(91L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Vehicle not found: 91");
    }

    @Test
    void getAllMapsVehiclesWithAssignedCarrier() {
        AppUser carrier = buildUser(7L, UserRole.CARRIER);
        when(vehicleRepository.findAllWithAssignedCarrierBy()).thenReturn(List.of(
                new Vehicle(
                        9L,
                        "TRUCK-9000",
                        new BigDecimal("6500.00"),
                        carrier,
                        Set.of()
                )
        ));

        List<VehicleResponse> responses = vehicleService.getAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getCarrier().getEmail()).isEqualTo("carrier7@test.local");
    }

    @Test
    void deleteExistingVehicleRemovesEntityAndInvalidatesCache() {
        when(vehicleRepository.existsById(12L)).thenReturn(true);

        vehicleService.delete(12L);

        verify(vehicleRepository).deleteById(12L);
        verify(shipmentSearchIndex).invalidateAll();
    }

    @Test
    void deleteMissingVehicleThrowsNotFound() {
        when(vehicleRepository.existsById(88L)).thenReturn(false);

        assertThatThrownBy(() -> vehicleService.delete(88L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Vehicle not found: 88");

        verify(vehicleRepository, never()).deleteById(88L);
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    private VehicleRequest request(String registrationNumber, String capacityKg, Long carrierId) {
        return new VehicleRequest(registrationNumber, new BigDecimal(capacityKg), carrierId);
    }

    private AppUser buildUser(Long id, UserRole role) {
        UserRoleLookup lookup = new UserRoleLookup(id, role.name());
        return new AppUser(
                id,
                "User",
                role.name(),
                role.name().toLowerCase() + id + "@test.local",
                lookup,
                List.of(),
                List.of(),
                List.of()
        );
    }
}
