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

        AppUserResponse response = appUserService.create(request(
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
    void createRejectsMissingRoleLookup() {
        when(userRoleLookupRepository.findByCode(UserRole.MANAGER.name()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.create(request(
                "Maksim",
                "Efimchik",
                "maksim@test.local",
                UserRole.MANAGER
        )))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Role lookup not found: MANAGER");

        verify(appUserRepository, never()).save(any(AppUser.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    @Test
    void updateChangesUserAndInvalidatesCache() {
        UserRoleLookup customerRole = new UserRoleLookup(1L, UserRole.CUSTOMER.name());
        UserRoleLookup managerRole = new UserRoleLookup(2L, UserRole.MANAGER.name());
        AppUser existingUser = new AppUser(
                9L,
                "Old",
                "Name",
                "old@test.local",
                customerRole,
                List.of(),
                List.of(),
                List.of()
        );
        when(appUserRepository.findById(9L)).thenReturn(Optional.of(existingUser));
        when(userRoleLookupRepository.findByCode(UserRole.MANAGER.name()))
                .thenReturn(Optional.of(managerRole));
        when(appUserRepository.save(existingUser)).thenReturn(existingUser);

        AppUserResponse response = appUserService.update(9L, request(
                "New",
                "Manager",
                "new-manager@test.local",
                UserRole.MANAGER
        ));

        assertThat(response.getId()).isEqualTo(9L);
        assertThat(response.getEmail()).isEqualTo("new-manager@test.local");
        assertThat(response.getRole()).isEqualTo(UserRole.MANAGER);
        assertThat(existingUser.getFirstName()).isEqualTo("New");
        assertThat(existingUser.getLastName()).isEqualTo("Manager");
        assertThat(existingUser.getRole()).isEqualTo(managerRole);
        verify(shipmentSearchIndex).invalidateAll();
    }

    @Test
    void updateMissingUserThrowsNotFound() {
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.update(99L, request(
                "Missing",
                "User",
                "missing@test.local",
                UserRole.CUSTOMER
        )))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found: 99");

        verify(appUserRepository, never()).save(any(AppUser.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    @Test
    void getByIdMapsExistingUser() {
        when(appUserRepository.findById(4L)).thenReturn(Optional.of(new AppUser(
                4L,
                "Ivan",
                "Ivanov",
                "ivan@test.local",
                new UserRoleLookup(1L, UserRole.CUSTOMER.name()),
                List.of(),
                List.of(),
                List.of()
        )));

        AppUserResponse response = appUserService.getById(4L);

        assertThat(response.getId()).isEqualTo(4L);
        assertThat(response.getEmail()).isEqualTo("ivan@test.local");
        assertThat(response.getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void getByIdMissingUserThrowsNotFound() {
        when(appUserRepository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.getById(55L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found: 55");
    }

    @Test
    void getAllMapsAllUsersToResponses() {
        UserRoleLookup customerRole = new UserRoleLookup(1L, UserRole.CUSTOMER.name());
        UserRoleLookup carrierRole = new UserRoleLookup(2L, UserRole.CARRIER.name());
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
                ),
                new AppUser(
                        2L,
                        "Oleg",
                        "Carrier",
                        "carrier@test.local",
                        carrierRole,
                        List.of(),
                        List.of(),
                        List.of()
                )
        ));

        List<AppUserResponse> responses = appUserService.getAll();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(AppUserResponse::getRole)
                .containsExactly(UserRole.CUSTOMER, UserRole.CARRIER);
    }

    @Test
    void deleteExistingUserRemovesEntityAndInvalidatesCache() {
        when(appUserRepository.existsById(7L)).thenReturn(true);

        appUserService.delete(7L);

        verify(appUserRepository).deleteById(7L);
        verify(shipmentSearchIndex).invalidateAll();
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

    private AppUserRequest request(String firstName, String lastName, String email, UserRole role) {
        return new AppUserRequest(firstName, lastName, email, role);
    }
}
