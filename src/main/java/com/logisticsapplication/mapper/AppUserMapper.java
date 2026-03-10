package com.logisticsapplication.mapper;

import com.logisticsapplication.dto.request.AppUserRequest;
import com.logisticsapplication.dto.response.AppUserResponse;
import com.logisticsapplication.model.AppUser;

public final class AppUserMapper {

    private AppUserMapper() {
    }

    public static AppUserResponse toResponse(AppUser user) {
        return new AppUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }

    public static void updateEntity(AppUser user, AppUserRequest request) {
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
    }
}
