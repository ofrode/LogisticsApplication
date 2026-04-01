package com.logisticsapplication.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание/обновление транспорта")
public class VehicleRequest {

    @NotBlank
    @Size(max = 30)
    @Schema(description = "Регистрационный номер", example = "AB-1234")
    private String registrationNumber;

    @NotNull
    @Positive
    @Schema(description = "Грузоподъемность (кг)", example = "5000.00")
    private BigDecimal capacityKg;

    @NotNull
    @Positive
    @Schema(description = "ID перевозчика", example = "3")
    private Long carrierId;
}
