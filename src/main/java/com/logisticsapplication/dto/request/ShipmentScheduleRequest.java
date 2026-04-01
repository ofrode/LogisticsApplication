package com.logisticsapplication.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
public class ShipmentScheduleRequest {

    @NotNull
    @Schema(description = "Дата/время создания заказа", example = "2026-03-24T10:00:00")
    private LocalDateTime orderCreatedAt;

    @NotNull
    @Schema(description = "Дата/время подтверждения заказа", example = "2026-03-24T12:00:00")
    private LocalDateTime orderReceivedAt;

    @NotNull
    @Schema(description = "Планируемое прибытие", example = "2026-03-27T14:00:00")
    private LocalDateTime arrivalAt;
}
