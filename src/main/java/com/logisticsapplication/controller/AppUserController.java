package com.logisticsapplication.controller;

import com.logisticsapplication.dto.request.AppUserRequest;
import com.logisticsapplication.dto.response.ApiErrorResponse;
import com.logisticsapplication.dto.response.AppUserResponse;
import com.logisticsapplication.service.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "Users", description = "Операции с пользователями")
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService appUserService;

    @PostMapping
    @Operation(summary = "Создать пользователя")
    @ApiResponse(responseCode = "200", description = "Пользователь создан")
    @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public ResponseEntity<AppUserResponse> create(@Valid @RequestBody AppUserRequest request) {
        return ResponseEntity.ok(appUserService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя")
    public ResponseEntity<AppUserResponse> update(
            @PathVariable @Positive(message = "id must be positive") Long id,
            @Valid @RequestBody AppUserRequest request
    ) {
        return ResponseEntity.ok(appUserService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по id")
    public ResponseEntity<AppUserResponse> getById(
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        return ResponseEntity.ok(appUserService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Получить список пользователей")
    public ResponseEntity<List<AppUserResponse>> getAll() {
        return ResponseEntity.ok(appUserService.getAll());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        appUserService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
