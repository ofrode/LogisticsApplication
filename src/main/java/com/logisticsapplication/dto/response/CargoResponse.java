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
@Schema(description = "Груз")
public class CargoResponse {

    @Schema(example = "10")
    private Long id;
    @Schema(example = "Electronics")
    private String name;
    @Schema(example = "1200.50")
    private BigDecimal weightKg;
}
