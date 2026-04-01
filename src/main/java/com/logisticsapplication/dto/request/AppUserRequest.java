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
@Schema(description = "Запрос на создание/обновление пользователя")
public class AppUserRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Имя", example = "Иван")
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Фамилия", example = "Иванов")
    private String lastName;

    @Email
    @NotBlank
    @Size(max = 255)
    @Schema(description = "Email", example = "ivan.ivanov@example.com")
    private String email;

    @NotNull
    @Schema(description = "Роль пользователя", example = "MANAGER")
    private UserRole role;
}
