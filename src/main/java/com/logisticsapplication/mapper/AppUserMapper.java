package com.logisticsapplication.mapper;

import com.logisticsapplication.dto.request.AppUserRequest;
import com.logisticsapplication.dto.response.AppUserResponse;
import com.logisticsapplication.model.AppUser;
import com.logisticsapplication.model.UserRole;

public final class AppUserMapper {

    private AppUserMapper() {
    }

    public static AppUserResponse toResponse(AppUser user) {
        return new AppUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                UserRole.valueOf(user.getRole().getCode())
        );
    }

    public static void updateEntity(AppUser user, AppUserRequest request) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
    }
}
