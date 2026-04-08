package com.logisticsapplication.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.logisticsapplication.dto.response.ShipmentResponse;
import com.logisticsapplication.model.AppUser;
import com.logisticsapplication.model.Cargo;
import com.logisticsapplication.model.Shipment;
import com.logisticsapplication.model.ShipmentSchedule;
import com.logisticsapplication.model.ShipmentStatus;
import com.logisticsapplication.model.ShipmentStatusLookup;
import com.logisticsapplication.model.UserRoleLookup;
import com.logisticsapplication.model.Vehicle;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ShipmentMapperTest {

    @Test
    void toResponseMapsFullShipment() {
        Shipment shipment = shipment(true);

        ShipmentResponse response = ShipmentMapper.toResponse(shipment);

        assertThat(response.getId()).isEqualTo(50L);
        assertThat(response.getTrackingNumber()).isEqualTo("SHIP-500");
        assertThat(response.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        assertThat(response.getCustomer().getEmail()).isEqualTo("customer@example.com");
        assertThat(response.getManager().getEmail()).isEqualTo("manager@example.com");
        assertThat(response.getCargoes()).hasSize(1);
        assertThat(response.getCargoes().get(0).getName()).isEqualTo("Paper");
        assertThat(response.getSchedule()).isNotNull();
        assertThat(response.getSchedule().getArrivalAt())
                .isEqualTo(LocalDateTime.of(2026, 4, 5, 15, 0));
        assertThat(response.getVehicles()).hasSize(1);
        assertThat(response.getVehicles().get(0).getRegistrationNumber()).isEqualTo("AB-1234");
    }

    @Test
    void toResponseReturnsNullScheduleWhenMissing() {
        Shipment shipment = shipment(false);

        ShipmentResponse response = ShipmentMapper.toResponse(shipment);

        assertThat(response.getSchedule()).isNull();
    }

    private Shipment shipment(boolean withSchedule) {
        UserRoleLookup customerRole = new UserRoleLookup(1L, "CUSTOMER");
        UserRoleLookup managerRole = new UserRoleLookup(2L, "MANAGER");
        ShipmentStatusLookup status = new ShipmentStatusLookup(1L, "IN_TRANSIT");

        AppUser customer = new AppUser(1L, "Ivan", "Petrov", "customer@example.com",
                customerRole, null, null, null);
        AppUser manager = new AppUser(2L, "Anna", "Manager", "manager@example.com",
                managerRole, null, null, null);
        AppUser carrier = new AppUser(3L, "Petr", "Carrier", "carrier@example.com",
                new UserRoleLookup(3L, "CARRIER"), null, null, null);

        Shipment shipment = new Shipment();
        shipment.setId(50L);
        shipment.setTrackingNumber("SHIP-500");
        shipment.setOriginCity("Minsk");
        shipment.setDestinationCity("Prague");
        shipment.setStatus(status);
        shipment.setCustomer(customer);
        shipment.setManager(manager);

        Cargo cargo = new Cargo(7L, "Paper", new BigDecimal("120.00"), null);
        shipment.addCargo(cargo);

        if (withSchedule) {
            shipment.setSchedule(new ShipmentSchedule(
                    8L,
                    LocalDateTime.of(2026, 4, 1, 10, 0),
                    LocalDateTime.of(2026, 4, 1, 12, 0),
                    LocalDateTime.of(2026, 4, 5, 15, 0),
                    null
            ));
        }

        Vehicle vehicle = new Vehicle(
                9L,
                "AB-1234",
                new BigDecimal("7000.00"),
                carrier,
                Set.of()
        );
        shipment.getVehicles().add(vehicle);
        return shipment;
    }
}
