package com.logisticsapplication.dto.response;

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
public class ShipmentResponse {

    private Integer id;
    private String cargoName;
    private String originCity;
    private String destinationCity;
    private LocalDate pickupDate;
    private BigDecimal weightKg;
    private String status;
}
