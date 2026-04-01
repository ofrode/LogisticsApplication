package com.logisticsapplication.dto.response;

import com.logisticsapplication.model.ShipmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Отправление")
public class ShipmentResponse {

    @Schema(example = "100")
    private Long id;
    @Schema(example = "SHIP-6001")
    private String trackingNumber;
    @Schema(example = "Minsk")
    private String originCity;
    @Schema(example = "Prague")
    private String destinationCity;
    @Schema(example = "IN_TRANSIT")
    private ShipmentStatus status;
    @Schema(description = "Клиент")
    private AppUserResponse customer;
    @Schema(description = "Менеджер")
    private AppUserResponse manager;
    @Schema(description = "Грузы")
    private List<CargoResponse> cargoes;
    @Schema(description = "Расписание")
    private ShipmentScheduleResponse schedule;
    @Schema(description = "Транспорт")
    private List<VehicleResponse> vehicles;
}
