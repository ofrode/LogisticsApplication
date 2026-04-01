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
@Schema(description = "Данные груза")
public class CargoRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Название груза", example = "Electronics")
    private String name;

    @NotNull
    @Positive
    @Schema(description = "Вес (кг)", example = "1200.5")
    private BigDecimal weightKg;
}
