package com.logisticsapplication.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentScheduleRequest {

    @NotNull
    private LocalDateTime orderCreatedAt;

    @NotNull
    private LocalDateTime orderReceivedAt;

    @NotNull
    private LocalDateTime arrivalAt;
}
