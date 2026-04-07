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

        VehicleResponse response = vehicleService.create(new VehicleRequest(
                "TRUCK-8080",
                new BigDecimal("7000.00"),
                3L
        ));

        assertThat(response.getId()).isEqualTo(8L);
        assertThat(response.getRegistrationNumber()).isEqualTo("TRUCK-8080");
        assertThat(response.getCarrier().getRole()).isEqualTo(UserRole.CARRIER);
        verify(shipmentSearchIndex).invalidateAll();
    }

    @Test
    void createRejectsAssignedUserWithoutCarrierRole() {
        VehicleRequest request = new VehicleRequest(
                "TRUCK-FAIL",
                new BigDecimal("5000.00"),
                4L
        );
        when(appUserRepository.findById(4L)).thenReturn(Optional.of(buildUser(4L, UserRole.CUSTOMER)));

        assertThatThrownBy(() -> vehicleService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Assigned user must have role CARRIER");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
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
                        java.util.Set.of()
                )
        ));

        List<VehicleResponse> responses = vehicleService.getAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getCarrier().getEmail()).isEqualTo("carrier7@test.local");
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
