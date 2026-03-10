package com.logisticsapplication.service;

import com.logisticsapplication.dto.request.AppUserRequest;
import com.logisticsapplication.dto.response.AppUserResponse;
import java.util.List;

public interface AppUserService {

    AppUserResponse create(AppUserRequest request);

    AppUserResponse update(Long id, AppUserRequest request);

    AppUserResponse getById(Long id);

    List<AppUserResponse> getAll();

    void delete(Long id);
}
