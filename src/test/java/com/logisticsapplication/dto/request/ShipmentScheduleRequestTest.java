package com.logisticsapplication.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ShipmentScheduleRequestTest {

    @Test
    void orderReceivedValidationAllowsNullValues() {
        ShipmentScheduleRequest request = new ShipmentScheduleRequest(
                null,
                LocalDateTime.of(2026, 4, 3, 11, 0),
                LocalDateTime.of(2026, 4, 3, 15, 0)
        );

        assertThat(request.isOrderReceivedAtValid()).isTrue();
    }

    @Test
    void orderReceivedValidationAllowsMissingReceivedDate() {
        ShipmentScheduleRequest request = new ShipmentScheduleRequest(
                LocalDateTime.of(2026, 4, 3, 10, 0),
                null,
                LocalDateTime.of(2026, 4, 3, 15, 0)
        );

        assertThat(request.isOrderReceivedAtValid()).isTrue();
    }

    @Test
    void orderReceivedValidationRejectsDateBeforeOrderCreated() {
        ShipmentScheduleRequest request = new ShipmentScheduleRequest(
                LocalDateTime.of(2026, 4, 3, 10, 0),
                LocalDateTime.of(2026, 4, 3, 9, 59),
                LocalDateTime.of(2026, 4, 4, 10, 0)
        );

        assertThat(request.isOrderReceivedAtValid()).isFalse();
    }

    @Test
    void orderReceivedValidationAcceptsSameOrLaterDate() {
        ShipmentScheduleRequest request = new ShipmentScheduleRequest(
                LocalDateTime.of(2026, 4, 3, 10, 0),
                LocalDateTime.of(2026, 4, 3, 10, 0),
                LocalDateTime.of(2026, 4, 4, 10, 0)
        );

        assertThat(request.isOrderReceivedAtValid()).isTrue();
    }

    @Test
    void arrivalValidationAllowsNullValues() {
        ShipmentScheduleRequest request = new ShipmentScheduleRequest(
                LocalDateTime.of(2026, 4, 3, 10, 0),
                null,
                null
        );

        assertThat(request.isArrivalAtValid()).isTrue();
    }

    @Test
    void arrivalValidationAllowsNullArrivalWhenOrderReceivedExists() {
        ShipmentScheduleRequest request = new ShipmentScheduleRequest(
                LocalDateTime.of(2026, 4, 3, 10, 0),
                LocalDateTime.of(2026, 4, 3, 12, 0),
                null
        );

        assertThat(request.isArrivalAtValid()).isTrue();
    }

    @Test
    void arrivalValidationRejectsDateBeforeOrderReceived() {
        ShipmentScheduleRequest request = new ShipmentScheduleRequest(
                LocalDateTime.of(2026, 4, 3, 10, 0),
                LocalDateTime.of(2026, 4, 3, 12, 0),
                LocalDateTime.of(2026, 4, 3, 11, 59)
        );

        assertThat(request.isArrivalAtValid()).isFalse();
    }

    @Test
    void arrivalValidationAcceptsSameOrLaterDate() {
        ShipmentScheduleRequest request = new ShipmentScheduleRequest(
                LocalDateTime.of(2026, 4, 3, 10, 0),
                LocalDateTime.of(2026, 4, 3, 12, 0),
                LocalDateTime.of(2026, 4, 3, 12, 0)
        );

        assertThat(request.isArrivalAtValid()).isTrue();
    }
}
