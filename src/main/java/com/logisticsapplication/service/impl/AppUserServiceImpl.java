package com.logisticsapplication.service.impl;

import com.logisticsapplication.dto.request.AppUserRequest;
import com.logisticsapplication.dto.response.AppUserResponse;
import com.logisticsapplication.mapper.AppUserMapper;
import com.logisticsapplication.model.AppUser;
import com.logisticsapplication.model.UserRoleLookup;
import com.logisticsapplication.repository.AppUserRepository;
import com.logisticsapplication.repository.UserRoleLookupRepository;
import com.logisticsapplication.service.AppUserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final UserRoleLookupRepository userRoleLookupRepository;

    @Override
    public AppUserResponse create(AppUserRequest request) {
        AppUser user = new AppUser();
        apply(user, request);
        return AppUserMapper.toResponse(appUserRepository.save(user));
    }

    @Override
    public AppUserResponse update(Long id, AppUserRequest request) {
        AppUser user = getEntity(id);
        apply(user, request);
        return AppUserMapper.toResponse(appUserRepository.save(user));
    }

    @Override
    public AppUserResponse getById(Long id) {
        return AppUserMapper.toResponse(getEntity(id));
    }

    @Override
    public List<AppUserResponse> getAll() {
        return appUserRepository.findAll().stream()
                .map(AppUserMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!appUserRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id);
        }
        appUserRepository.deleteById(id);
    }

    private AppUser getEntity(Long id) {
        return appUserRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id)
        );
    }

    private void apply(AppUser user, AppUserRequest request) {
        AppUserMapper.updateEntity(user, request);
        UserRoleLookup role = userRoleLookupRepository.findByCode(request.getRole().name())
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Role lookup not found: " + request.getRole().name()
                        )
                );
        user.setRole(role);
    }
}
