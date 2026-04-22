package com.logisticsapplication.dto.response;

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
@Schema(description = "Сводка по всем асинхронным задачам, сгруппированным по статусам")
public class AsyncShipmentTaskOverviewResponse {

    @Schema(description = "Отправленные задачи, которые ещё не начали выполняться")
    private List<AsyncShipmentTaskStatusResponse> submittedTasks;

    @Schema(description = "Задачи, которые сейчас обрабатываются")
    private List<AsyncShipmentTaskStatusResponse> processingTasks;

    @Schema(description = "Успешно завершённые задачи")
    private List<AsyncShipmentTaskStatusResponse> completedTasks;

    @Schema(description = "Задачи, завершившиеся с ошибкой")
    private List<AsyncShipmentTaskStatusResponse> failedTasks;
}
