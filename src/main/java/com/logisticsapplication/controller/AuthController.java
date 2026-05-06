package com.logisticsapplication.controller;

import com.logisticsapplication.dto.request.AuthLoginRequest;
import com.logisticsapplication.dto.request.AuthRegisterRequest;
import com.logisticsapplication.dto.response.ApiErrorResponse;
import com.logisticsapplication.dto.response.AppUserResponse;
import com.logisticsapplication.dto.response.AuthLoginResponse;
import com.logisticsapplication.service.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Авторизация и публичная регистрация")
public class AuthController {

    private final AppUserService appUserService;

    @PostMapping("/login")
    @Operation(summary = "Авторизовать пользователя")
    @ApiResponse(responseCode = "200", description = "Авторизация успешна")
    @ApiResponse(
            responseCode = "401",
            description = "Неверный логин или пароль",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public ResponseEntity<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        return ResponseEntity.ok(appUserService.authenticate(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Публичная регистрация пользователя")
    @ApiResponse(responseCode = "200", description = "Пользователь зарегистрирован")
    @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public ResponseEntity<AppUserResponse> register(
            @Valid @RequestBody AuthRegisterRequest request
    ) {
        AppUserResponse response = appUserService.register(request);
        return ResponseEntity.ok(response);
    }
}
