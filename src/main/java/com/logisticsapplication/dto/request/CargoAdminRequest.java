package com.logisticsapplication.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
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
@Schema(description = "Запрос на обновление груза администратором")
public class CargoAdminRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Название груза", example = "Electronics")
    private String name;

    @NotNull
    @Positive
    @DecimalMax(value = "30000", message = "weightKg must be less than or equal to 30000")
    @Schema(description = "Вес (кг), максимум 30000", example = "1200.5")
    private BigDecimal weightKg;

    @NotNull
    @Positive
    @Schema(description = "ID заявки", example = "10")
    private Long shipmentId;
}
