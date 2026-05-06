package com.logisticsapplication.dto.request;

import com.logisticsapplication.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Публичная регистрация пользователя")
public class AuthRegisterRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Имя", example = "Анна")
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Фамилия", example = "Иванова")
    private String lastName;

    @Email
    @NotBlank
    @Size(max = 255)
    @Schema(description = "Email", example = "anna@example.com")
    private String email;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Логин пользователя", example = "anna_customer")
    private String login;

    @NotBlank
    @Size(min = 8, max = 255)
    @Schema(description = "Пароль пользователя", example = "StrongPass123")
    private String password;

    @NotNull
    @Schema(description = "Роль пользователя", example = "CUSTOMER")
    private UserRole role;
}
