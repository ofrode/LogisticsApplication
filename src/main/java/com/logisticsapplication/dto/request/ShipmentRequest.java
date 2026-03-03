package com.logisticsapplication.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRequest {

    @NotBlank
    private String cargoName;

    @NotBlank
    private String originCity;

    @NotBlank
    private String destinationCity;

    @NotNull
    private LocalDate pickupDate;

    @NotNull
    @Positive
    private BigDecimal weightKg;

    @NotBlank
    private String status;
}
