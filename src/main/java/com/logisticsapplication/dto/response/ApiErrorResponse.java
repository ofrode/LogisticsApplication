package com.logisticsapplication.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Единый формат ошибки API")
public class ApiErrorResponse {

    @Schema(description = "Время возникновения ошибки в UTC", example = "2026-04-01T08:11:13Z")
    private Instant timestamp;

    @Schema(description = "HTTP статус", example = "400")
    private int status;

    @Schema(description = "Короткое имя ошибки", example = "BAD_REQUEST")
    private String error;

    @Schema(description = "Общее сообщение ошибки", example = "Validation failed")
    private String message;

    @Schema(description = "Путь запроса", example = "/api/shipments")
    private String path;

    @Schema(description = "Ошибки по полям запроса")
    private Map<String, String> fieldErrors;
}
