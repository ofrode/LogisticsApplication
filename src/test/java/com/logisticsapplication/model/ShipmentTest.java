package com.logisticsapplication.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ShipmentTest {

    @Test
    void addCargoAttachesCargoToShipment() {
        Shipment shipment = new Shipment();
        Cargo cargo = new Cargo(1L, "Paper", new BigDecimal("10.00"), null);

        shipment.addCargo(cargo);

        assertThat(shipment.getCargoes()).containsExactly(cargo);
        assertThat(cargo.getShipment()).isSameAs(shipment);
    }

    @Test
    void clearCargoesDetachesAndClearsAllCargoes() {
        Shipment shipment = new Shipment();
        Cargo firstCargo = new Cargo(1L, "Paper", new BigDecimal("10.00"), null);
        Cargo secondCargo = new Cargo(2L, "Boxes", new BigDecimal("20.00"), null);
        shipment.addCargo(firstCargo);
        shipment.addCargo(secondCargo);

        shipment.clearCargoes();

        assertThat(shipment.getCargoes()).isEmpty();
        assertThat(firstCargo.getShipment()).isNull();
        assertThat(secondCargo.getShipment()).isNull();
    }

    @Test
    void setScheduleReplacesExistingRelationAndSupportsNull() {
        Shipment shipment = new Shipment();
        ShipmentSchedule firstSchedule = schedule(1L, 1);
        ShipmentSchedule secondSchedule = schedule(2L, 2);

        shipment.setSchedule(firstSchedule);
        shipment.setSchedule(secondSchedule);

        assertThat(firstSchedule.getShipment()).isNull();
        assertThat(secondSchedule.getShipment()).isSameAs(shipment);
        assertThat(shipment.getSchedule()).isSameAs(secondSchedule);

        shipment.setSchedule(null);

        assertThat(secondSchedule.getShipment()).isNull();
        assertThat(shipment.getSchedule()).isNull();
    }

    private ShipmentSchedule schedule(Long id, int day) {
        return new ShipmentSchedule(
                id,
                LocalDateTime.of(2026, 4, day, 10, 0),
                LocalDateTime.of(2026, 4, day, 12, 0),
                LocalDateTime.of(2026, 4, day + 1, 12, 0),
                null
        );
    }
}
