package com.logisticsapplication.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Транспорт")
public class VehicleResponse {

    @Schema(example = "5")
    private Long id;
    @Schema(example = "EF-9011")
    private String registrationNumber;
    @Schema(example = "7000.00")
    private BigDecimal capacityKg;
    @Schema(description = "Назначенный перевозчик")
    private AppUserResponse carrier;
}
