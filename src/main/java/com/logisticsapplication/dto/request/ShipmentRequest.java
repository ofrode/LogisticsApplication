package com.logisticsapplication.dto.request;

import com.logisticsapplication.model.ShipmentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRequest {

    @NotBlank
    private String trackingNumber;

    @NotBlank
    private String originCity;

    @NotBlank
    private String destinationCity;

    @NotNull
    private ShipmentStatus status;

    @NotNull
    private Long customerId;

    @NotNull
    private Long managerId;

    @NotEmpty
    private List<Long> vehicleIds;

    @Valid
    @NotEmpty
    private List<CargoRequest> cargoes;

    @Valid
    @NotNull
    private ShipmentScheduleRequest schedule;
}
