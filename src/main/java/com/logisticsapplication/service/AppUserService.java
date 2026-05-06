package com.logisticsapplication.service;

import com.logisticsapplication.dto.request.AppUserRequest;
import com.logisticsapplication.dto.request.AuthLoginRequest;
import com.logisticsapplication.dto.request.AuthRegisterRequest;
import com.logisticsapplication.dto.response.AppUserResponse;
import com.logisticsapplication.dto.response.AuthLoginResponse;
import java.util.List;

public interface AppUserService {

    AppUserResponse create(AppUserRequest request);

    AppUserResponse register(AuthRegisterRequest request);

    AuthLoginResponse authenticate(AuthLoginRequest request);

    AppUserResponse update(Long id, AppUserRequest request);

    AppUserResponse getById(Long id);

    List<AppUserResponse> getAll();

    void delete(Long id);
}
