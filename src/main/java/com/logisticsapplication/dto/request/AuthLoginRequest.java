package com.logisticsapplication.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на авторизацию пользователя")
public class AuthLoginRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Логин пользователя", example = "ivan_admin")
    private String login;

    @NotBlank
    @Size(min = 8, max = 255)
    @Schema(description = "Пароль пользователя", example = "StrongPass123")
    private String password;
}
