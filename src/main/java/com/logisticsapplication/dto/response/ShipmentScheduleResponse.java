package com.logisticsapplication.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Расписание отправления")
public class ShipmentScheduleResponse {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "2026-03-24T10:00:00")
    private LocalDateTime orderCreatedAt;
    @Schema(example = "2026-03-24T12:00:00")
    private LocalDateTime orderReceivedAt;
    @Schema(example = "2026-03-27T14:00:00")
    private LocalDateTime arrivalAt;
}
