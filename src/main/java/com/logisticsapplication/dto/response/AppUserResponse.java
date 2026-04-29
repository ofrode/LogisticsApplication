package com.logisticsapplication.dto.response;

import com.logisticsapplication.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Пользователь")
public class AppUserResponse {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "Иван")
    private String firstName;
    @Schema(example = "Иванов")
    private String lastName;
    @Schema(example = "ivan.ivanov@example.com")
    private String email;
    @Schema(
            description = "Логин пользователя. Совпадает с email.",
            example = "ivan.ivanov@example.com"
    )
    private String login;
    @Schema(example = "MANAGER")
    private UserRole role;
}
