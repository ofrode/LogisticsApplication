package com.logisticsapplication.service.impl;

import com.logisticsapplication.cache.ShipmentSearchIndex;
import com.logisticsapplication.dto.request.AppUserRequest;
import com.logisticsapplication.dto.response.AppUserResponse;
import com.logisticsapplication.model.AppUser;
import com.logisticsapplication.model.UserRole;
import com.logisticsapplication.model.UserRoleLookup;
import com.logisticsapplication.repository.AppUserRepository;
import com.logisticsapplication.repository.UserRoleLookupRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private UserRoleLookupRepository userRoleLookupRepository;

    @Mock
    private ShipmentSearchIndex shipmentSearchIndex;

    @InjectMocks
    private AppUserServiceImpl appUserService;

    @Test
    void createAssignsRoleAndInvalidatesCache() {
        UserRoleLookup managerRole = new UserRoleLookup(2L, UserRole.MANAGER.name());
        when(userRoleLookupRepository.findByCode(UserRole.MANAGER.name()))
                .thenReturn(Optional.of(managerRole));
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(15L);
            return user;
        });

        AppUserResponse response = appUserService.create(new AppUserRequest(
                "Maksim",
                "Efimchik",
                "maksim@test.local",
                UserRole.MANAGER
        ));

        assertThat(response.getId()).isEqualTo(15L);
        assertThat(response.getEmail()).isEqualTo("maksim@test.local");
        assertThat(response.getRole()).isEqualTo(UserRole.MANAGER);
        verify(shipmentSearchIndex).invalidateAll();
    }

    @Test
    void getAllMapsAllUsersToResponses() {
        UserRoleLookup customerRole = new UserRoleLookup(1L, UserRole.CUSTOMER.name());
        when(appUserRepository.findAll()).thenReturn(List.of(
                new AppUser(
                        1L,
                        "Ivan",
                        "Ivanov",
                        "ivan@test.local",
                        customerRole,
                        List.of(),
                        List.of(),
                        List.of()
                )
        ));

        List<AppUserResponse> responses = appUserService.getAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void deleteMissingUserThrowsNotFound() {
        when(appUserRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> appUserService.delete(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found: 99");

        verify(appUserRepository, never()).deleteById(99L);
        verify(shipmentSearchIndex, never()).invalidateAll();
    }
}
