package com.logisticsapplication.dto.response;

import com.logisticsapplication.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppUserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
}
