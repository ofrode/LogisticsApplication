package com.logisticsapplication.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Пагинированный ответ")
public class PageResponse<T> {

    @Schema(description = "Данные страницы")
    private final List<T> content;
    @Schema(example = "0")
    private final int page;
    @Schema(example = "10")
    private final int size;
    @Schema(example = "25")
    private final long totalElements;
    @Schema(example = "3")
    private final int totalPages;
    @Schema(description = "Ответ получен из кеша", example = "false")
    private final boolean fromCache;
    @Schema(description = "Тип поискового запроса", example = "JPQL")
    private final String queryType;
}
