package com.logisticsapplication.dto.response;

import com.logisticsapplication.model.AsyncTaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Статус асинхронной задачи по созданию отправлений")
public class AsyncShipmentTaskStatusResponse {

    @Schema(example = "1")
    private Long taskId;

    @Schema(example = "RUNNING")
    private AsyncTaskStatus status;

    @Schema(example = "3")
    private int requestedShipments;

    @Schema(example = "3")
    private int processedShipments;

    @Schema(description = "ID созданных отправлений")
    private List<Long> createdShipmentIds;

    @Schema(example = "Tracking number already exists: SHIP-100")
    private String errorMessage;

    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
}
