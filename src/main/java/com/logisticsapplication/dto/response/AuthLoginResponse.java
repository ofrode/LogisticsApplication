package com.logisticsapplication.dto.response;

import com.logisticsapplication.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Результат авторизации")
public class AuthLoginResponse {

    @Schema(description = "Информация о пользователе")
    private AppUserResponse user;

    @Schema(description = "Роль пользователя", example = "MANAGER")
    private UserRole role;

    @Schema(description = "Страница роли для перехода", example = "/manager.html")
    private String redirectUrl;

    @Schema(description = "JWT токен доступа")
    private String token;
}
