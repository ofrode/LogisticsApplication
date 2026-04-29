package com.logisticsapplication.service.impl;

import com.logisticsapplication.cache.ShipmentSearchIndex;
import com.logisticsapplication.dto.request.AppUserRequest;
import com.logisticsapplication.dto.response.AppUserResponse;
import com.logisticsapplication.mapper.AppUserMapper;
import com.logisticsapplication.model.AppUser;
import com.logisticsapplication.model.UserRoleLookup;
import com.logisticsapplication.repository.AppUserRepository;
import com.logisticsapplication.repository.UserRoleLookupRepository;
import com.logisticsapplication.service.AppUserService;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final UserRoleLookupRepository userRoleLookupRepository;
    private final ShipmentSearchIndex shipmentSearchIndex;

    @Override
    public AppUserResponse create(AppUserRequest request) {
        ensureEmailAvailable(request.getEmail(), null);
        AppUser user = new AppUser();
        apply(user, request);
        AppUserResponse response = AppUserMapper.toResponse(appUserRepository.save(user));
        shipmentSearchIndex.invalidateAll();
        return response;
    }

    @Override
    public AppUserResponse update(Long id, AppUserRequest request) {
        AppUser user = getEntity(id);
        ensureEmailAvailable(request.getEmail(), id);
        apply(user, request);
        AppUserResponse response = AppUserMapper.toResponse(appUserRepository.save(user));
        shipmentSearchIndex.invalidateAll();
        return response;
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
        shipmentSearchIndex.invalidateAll();
    }

    private AppUser getEntity(Long id) {
        return appUserRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id)
        );
    }

    private void apply(AppUser user, AppUserRequest request) {
        AppUserMapper.updateEntity(user, request);
        user.setEmail(normalizeEmail(request.getEmail()));
        UserRoleLookup role = userRoleLookupRepository.findByCode(request.getRole().name())
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Role lookup not found: " + request.getRole().name()
                        )
                );
        user.setRole(role);
    }

    private void ensureEmailAvailable(String email, Long currentUserId) {
        String normalizedEmail = normalizeEmail(email);
        appUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(existingUser -> !Objects.equals(existingUser.getId(), currentUserId))
                .ifPresent(existingUser -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "User login already exists: " + normalizedEmail
                    );
                });
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
