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
@Schema(description = "Снимок потокобезопасного счётчика")
public class CounterSnapshotResponse {

    @Schema(example = "ATOMIC")
    private String counterType;

    @Schema(example = "25")
    private int currentValue;
}
