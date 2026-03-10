package com.logisticsapplication.dto.response;

import com.logisticsapplication.model.ShipmentStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {

    private Long id;
    private String trackingNumber;
    private String originCity;
    private String destinationCity;
    private ShipmentStatus status;
    private AppUserResponse customer;
    private AppUserResponse manager;
    private List<CargoResponse> cargoes;
    private ShipmentScheduleResponse schedule;
    private List<VehicleResponse> vehicles;
}
