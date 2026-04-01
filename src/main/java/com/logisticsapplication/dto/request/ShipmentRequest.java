package com.logisticsapplication.dto.request;

import com.logisticsapplication.model.ShipmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание/обновление отправления")
public class ShipmentRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Трек-номер", example = "SHIP-6001")
    private String trackingNumber;

    @NotBlank
    @Size(max = 150)
    @Schema(description = "Город отправления", example = "Minsk")
    private String originCity;

    @NotBlank
    @Size(max = 150)
    @Schema(description = "Город назначения", example = "Prague")
    private String destinationCity;

    @NotNull
    @Schema(description = "Статус отправления", example = "CREATED")
    private ShipmentStatus status;

    @NotNull
    @Positive
    @Schema(description = "ID клиента", example = "1")
    private Long customerId;

    @NotNull
    @Positive
    @Schema(description = "ID менеджера", example = "2")
    private Long managerId;

    @NotEmpty
    @Schema(description = "Список ID транспорта")
    private List<@Positive Long> vehicleIds;

    @Valid
    @NotEmpty
    @Schema(description = "Список грузов")
    private List<CargoRequest> cargoes;

    @Valid
    @NotNull
    @Schema(description = "Расписание отправления")
    private ShipmentScheduleRequest schedule;
}
