package com.logisticsapplication.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Результат демонстрации race condition")
public class RaceConditionDemoResponse {

    @Schema(example = "64")
    private int threadCount;

    @Schema(example = "5000")
    private int incrementsPerThread;

    @Schema(example = "320000")
    private int expectedValue;

    @Schema(example = "245163")
    private int unsafeValue;

    @Schema(example = "320000")
    private int atomicValue;

    @Schema(example = "320000")
    private int synchronizedValue;

    @Schema(example = "74837")
    private int lostUpdates;
}
