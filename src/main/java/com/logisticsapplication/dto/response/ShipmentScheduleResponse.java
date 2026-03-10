package com.logisticsapplication.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentScheduleResponse {

    private Long id;
    private LocalDateTime orderCreatedAt;
    private LocalDateTime orderReceivedAt;
    private LocalDateTime arrivalAt;
}
