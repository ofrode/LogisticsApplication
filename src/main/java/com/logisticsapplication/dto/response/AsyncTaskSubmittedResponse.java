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
@Schema(description = "Ответ после запуска асинхронной задачи")
public class AsyncTaskSubmittedResponse {

    @Schema(example = "1")
    private Long taskId;

    @Schema(example = "PENDING")
    private String status;
}
