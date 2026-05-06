package com.logisticsapplication.service.impl;

import com.logisticsapplication.cache.ShipmentSearchIndex;
import com.logisticsapplication.dto.request.AppUserRequest;
import com.logisticsapplication.dto.request.AuthLoginRequest;
import com.logisticsapplication.dto.request.AuthRegisterRequest;
import com.logisticsapplication.dto.response.AppUserResponse;
import com.logisticsapplication.dto.response.AuthLoginResponse;
import com.logisticsapplication.mapper.AppUserMapper;
import com.logisticsapplication.model.AppUser;
import com.logisticsapplication.model.UserRole;
import com.logisticsapplication.model.UserRoleLookup;
import com.logisticsapplication.repository.AppUserRepository;
import com.logisticsapplication.repository.UserRoleLookupRepository;
import com.logisticsapplication.service.AppUserService;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final AppUserRepository appUserRepository;
    private final UserRoleLookupRepository userRoleLookupRepository;
    private final ShipmentSearchIndex shipmentSearchIndex;

    @Override
    public AppUserResponse create(AppUserRequest request) {
        ensureEmailAvailable(request.getEmail(), null);
        ensureLoginAvailable(request.getLogin(), null);
        AppUser user = new AppUser();
        apply(
                user,
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getLogin(),
                request.getPassword(),
                request.getRole()
        );
        AppUserResponse response = AppUserMapper.toResponse(appUserRepository.save(user));
        shipmentSearchIndex.invalidateAll();
        return response;
    }

    @Override
    public AppUserResponse register(AuthRegisterRequest request) {
        validatePublicRole(request.getRole());
        ensureEmailAvailable(request.getEmail(), null);
        ensureLoginAvailable(request.getLogin(), null);
        AppUser user = new AppUser();
        apply(
                user,
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getLogin(),
                request.getPassword(),
                request.getRole()
        );
        AppUserResponse response = AppUserMapper.toResponse(appUserRepository.save(user));
        shipmentSearchIndex.invalidateAll();
        return response;
    }

    @Override
    public AuthLoginResponse authenticate(AuthLoginRequest request) {
        String normalizedLogin = normalizeLogin(request.getLogin());
        AppUser user = appUserRepository.findByLoginIgnoreCase(normalizedLogin)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid login or password"
                        )
                );
        if (!PASSWORD_ENCODER.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid login or password");
        }
        AppUserResponse userResponse = AppUserMapper.toResponse(user);
        UserRole role = userResponse.getRole();
        return new AuthLoginResponse(userResponse, role, resolveRedirectUrl(role));
    }

    @Override
    public AppUserResponse update(Long id, AppUserRequest request) {
        AppUser user = getEntity(id);
        ensureEmailAvailable(request.getEmail(), id);
        ensureLoginAvailable(request.getLogin(), id);
        apply(
                user,
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getLogin(),
                request.getPassword(),
                request.getRole()
        );
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

    private void apply(
            AppUser user,
            String firstName,
            String lastName,
            String email,
            String login,
            String password,
            UserRole role
    ) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(normalizeEmail(email));
        user.setLogin(normalizeLogin(login));
        user.setPasswordHash(PASSWORD_ENCODER.encode(password));
        UserRoleLookup roleLookup = userRoleLookupRepository.findByCode(role.name())
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Role lookup not found: " + role.name()
                        )
                );
        user.setRole(roleLookup);
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

    private void ensureLoginAvailable(String login, Long currentUserId) {
        String normalizedLogin = normalizeLogin(login);
        appUserRepository.findByLoginIgnoreCase(normalizedLogin)
                .filter(existingUser -> !Objects.equals(existingUser.getId(), currentUserId))
                .ifPresent(existingUser -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "User login already exists: " + normalizedLogin
                    );
                });
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeLogin(String login) {
        return login.trim().toLowerCase(Locale.ROOT);
    }

    private void validatePublicRole(UserRole role) {
        if (role == UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Public registration does not allow ADMIN role"
            );
        }
    }

    private String resolveRedirectUrl(UserRole role) {
        return switch (role) {
            case ADMIN -> "/admin.html";
            case MANAGER -> "/manager.html";
            case CUSTOMER -> "/customer.html";
            case CARRIER -> "/carrier.html";
        };
    }
}
