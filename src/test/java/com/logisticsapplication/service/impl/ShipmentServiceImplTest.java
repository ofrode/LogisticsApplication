package com.logisticsapplication.service.impl;

import com.logisticsapplication.cache.ShipmentSearchIndex;
import com.logisticsapplication.dto.request.CargoRequest;
import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.request.ShipmentScheduleRequest;
import com.logisticsapplication.dto.response.PageResponse;
import com.logisticsapplication.dto.response.ShipmentResponse;
import com.logisticsapplication.model.AppUser;
import com.logisticsapplication.model.Cargo;
import com.logisticsapplication.model.Shipment;
import com.logisticsapplication.model.ShipmentSearchQueryType;
import com.logisticsapplication.model.ShipmentSchedule;
import com.logisticsapplication.model.ShipmentStatus;
import com.logisticsapplication.model.ShipmentStatusLookup;
import com.logisticsapplication.model.UserRole;
import com.logisticsapplication.model.UserRoleLookup;
import com.logisticsapplication.model.Vehicle;
import com.logisticsapplication.repository.AppUserRepository;
import com.logisticsapplication.repository.CargoRepository;
import com.logisticsapplication.repository.ShipmentRepository;
import com.logisticsapplication.repository.ShipmentScheduleRepository;
import com.logisticsapplication.repository.ShipmentStatusLookupRepository;
import com.logisticsapplication.repository.VehicleRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceImplTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ShipmentScheduleRepository shipmentScheduleRepository;

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private ShipmentStatusLookupRepository shipmentStatusLookupRepository;

    @Mock
    private ShipmentSearchIndex shipmentSearchIndex;

    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    private AppUser customer;
    private AppUser manager;
    private AppUser carrier;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        customer = user(10L, "Ivan", UserRole.CUSTOMER);
        manager = user(11L, "Petr", UserRole.MANAGER);
        carrier = user(12L, "Oleg", UserRole.CARRIER);
        vehicle = new Vehicle(20L, "TRUCK-20", decimal("4500.00"), carrier, Set.of());
    }

    @Test
    void createSavesShipmentAndInvalidatesCache() {
        stubSuccessfulAggregateResolution(ShipmentStatus.CREATED);
        when(shipmentRepository.findByTrackingNumber("SHIP-001")).thenReturn(Optional.empty());
        stubShipmentSaveAssignsIds();

        ShipmentResponse response = shipmentService.create(buildRequest("SHIP-001", ShipmentStatus.CREATED));

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTrackingNumber()).isEqualTo("SHIP-001");
        assertThat(response.getStatus()).isEqualTo(ShipmentStatus.CREATED);
        assertThat(response.getCargoes()).hasSize(1);
        assertThat(response.getSchedule()).isNotNull();
        verify(shipmentRepository).save(any(Shipment.class));
        verify(shipmentSearchIndex).invalidateAll();
    }

    @Test
    void createRejectsExistingTrackingNumber() {
        Shipment existingShipment = shipment(77L, "SHIP-EXISTS", ShipmentStatus.CREATED, "Minsk", "Warsaw");
        when(shipmentRepository.findByTrackingNumber("SHIP-EXISTS"))
                .thenReturn(Optional.of(existingShipment));

        assertThatThrownBy(() -> shipmentService.create(buildRequest("SHIP-EXISTS", ShipmentStatus.CREATED)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tracking number already exists: SHIP-EXISTS");

        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    @Test
    void createRejectsMissingCustomer() {
        when(shipmentRepository.findByTrackingNumber("SHIP-002")).thenReturn(Optional.empty());
        when(shipmentStatusLookupRepository.findByCode(ShipmentStatus.CREATED.name()))
                .thenReturn(Optional.of(statusLookup(ShipmentStatus.CREATED)));
        when(appUserRepository.findById(customer.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.create(buildRequest("SHIP-002", ShipmentStatus.CREATED)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Customer not found: 10");

        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    @Test
    void createRejectsCustomerWithWrongRole() {
        when(shipmentRepository.findByTrackingNumber("SHIP-003")).thenReturn(Optional.empty());
        when(appUserRepository.findById(customer.getId())).thenReturn(Optional.of(carrier));
        when(shipmentStatusLookupRepository.findByCode(ShipmentStatus.CREATED.name()))
                .thenReturn(Optional.of(statusLookup(ShipmentStatus.CREATED)));

        assertThatThrownBy(() -> shipmentService.create(buildRequest("SHIP-003", ShipmentStatus.CREATED)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Customer must have role CUSTOMER");

        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    @Test
    void createRejectsMissingVehicle() {
        stubUsers();
        when(shipmentRepository.findByTrackingNumber("SHIP-004")).thenReturn(Optional.empty());
        when(vehicleRepository.findAllById(List.of(vehicle.getId()))).thenReturn(List.of());
        when(shipmentStatusLookupRepository.findByCode(ShipmentStatus.CREATED.name()))
                .thenReturn(Optional.of(statusLookup(ShipmentStatus.CREATED)));

        assertThatThrownBy(() -> shipmentService.create(buildRequest("SHIP-004", ShipmentStatus.CREATED)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Some vehicles were not found");

        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    @Test
    void createRejectsMissingStatusLookup() {
        when(shipmentRepository.findByTrackingNumber("SHIP-005")).thenReturn(Optional.empty());
        when(shipmentStatusLookupRepository.findByCode(ShipmentStatus.CREATED.name()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.create(buildRequest("SHIP-005", ShipmentStatus.CREATED)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Shipment status lookup not found: CREATED");

        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    @Test
    void createBulkCreatesAllShipmentsAndInvalidatesCache() {
        stubSuccessfulAggregateResolution(ShipmentStatus.CREATED, ShipmentStatus.RECEIVED);
        when(shipmentRepository.findByTrackingNumber(anyString())).thenReturn(Optional.empty());
        stubShipmentSaveAssignsIds();

        List<ShipmentResponse> responses = shipmentService.createBulk(List.of(
                buildRequest("BULK-001", ShipmentStatus.CREATED),
                buildRequest("BULK-002", ShipmentStatus.RECEIVED)
        ));

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ShipmentResponse::getTrackingNumber)
                .containsExactly("BULK-001", "BULK-002");
        assertThat(responses).extracting(ShipmentResponse::getStatus)
                .containsExactly(ShipmentStatus.CREATED, ShipmentStatus.RECEIVED);
        verify(shipmentRepository, times(2)).save(any(Shipment.class));
        verify(shipmentSearchIndex).invalidateAll();
    }

    @Test
    void createBulkRejectsDuplicateTrackingNumbersInsideRequestList() {
        List<ShipmentRequest> requests = List.of(
                buildRequest("DUPLICATE-001", ShipmentStatus.CREATED),
                buildRequest("DUPLICATE-001", ShipmentStatus.RECEIVED)
        );

        assertThatThrownBy(() -> shipmentService.createBulk(requests))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("duplicate tracking numbers");

        verify(shipmentRepository, never()).findByTrackingNumber(anyString());
        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    @Test
    void createBulkRejectsTrackingNumberThatAlreadyExistsInDatabase() {
        Shipment existingShipment = shipment(77L, "BULK-EXISTS", ShipmentStatus.CREATED, "Minsk", "Warsaw");
        when(shipmentRepository.findByTrackingNumber("BULK-EXISTS"))
                .thenReturn(Optional.of(existingShipment));

        assertThatThrownBy(() -> shipmentService.createBulk(List.of(
                buildRequest("BULK-EXISTS", ShipmentStatus.CREATED)
        )))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tracking number already exists");

        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    @Test
    void updateExistingShipmentRefreshesCargoesAndScheduleAndInvalidatesCache() {
        Shipment existingShipment = shipment(5L, "SHIP-UPDATE", ShipmentStatus.CREATED, "Minsk", "Prague");
        Cargo oldCargo = existingShipment.getCargoes().getFirst();
        ShipmentSchedule existingSchedule = existingShipment.getSchedule();
        ShipmentRequest request = buildRequest(
                "SHIP-UPDATE",
                ShipmentStatus.RECEIVED,
                List.of(
                        cargo("Paper", "200.00"),
                        cargo("Glass", "300.00")
                ),
                schedule(5),
                List.of(vehicle.getId()),
                customer.getId(),
                manager.getId()
        );
        when(shipmentRepository.findDetailedById(5L)).thenReturn(Optional.of(existingShipment));
        when(shipmentRepository.findByTrackingNumber("SHIP-UPDATE"))
                .thenReturn(Optional.of(existingShipment));
        stubSuccessfulAggregateResolution(ShipmentStatus.RECEIVED);
        when(shipmentRepository.save(existingShipment)).thenReturn(existingShipment);

        ShipmentResponse response = shipmentService.update(5L, request);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getStatus()).isEqualTo(ShipmentStatus.RECEIVED);
        assertThat(response.getCargoes()).hasSize(2);
        assertThat(existingShipment.getCargoes()).hasSize(2);
        assertThat(oldCargo.getShipment()).isNull();
        assertThat(existingShipment.getSchedule()).isSameAs(existingSchedule);
        assertThat(existingSchedule.getArrivalAt()).isEqualTo(request.getSchedule().getArrivalAt());
        verify(shipmentSearchIndex).invalidateAll();
    }

    @Test
    void updateRejectsTrackingNumberOwnedByAnotherShipment() {
        Shipment targetShipment = shipment(5L, "SHIP-005", ShipmentStatus.CREATED, "Minsk", "Prague");
        Shipment otherShipment = shipment(6L, "SHIP-OTHER", ShipmentStatus.CREATED, "Vilnius", "Riga");
        when(shipmentRepository.findDetailedById(5L)).thenReturn(Optional.of(targetShipment));
        when(shipmentRepository.findByTrackingNumber("SHIP-NEW"))
                .thenReturn(Optional.of(otherShipment));

        assertThatThrownBy(() -> shipmentService.update(5L, buildRequest("SHIP-NEW", ShipmentStatus.CREATED)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tracking number already exists: SHIP-NEW");

        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    @Test
    void updateMissingShipmentThrowsNotFound() {
        when(shipmentRepository.findDetailedById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.update(99L, buildRequest("SHIP-099", ShipmentStatus.CREATED)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Shipment not found: 99");

        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    @Test
    void getByIdReturnsDetailedShipment() {
        Shipment shipment = shipment(50L, "SHIP-050", ShipmentStatus.RECEIVED, "Minsk", "Warsaw");
        when(shipmentRepository.findDetailedById(50L)).thenReturn(Optional.of(shipment));

        ShipmentResponse response = shipmentService.getById(50L);

        assertThat(response.getId()).isEqualTo(50L);
        assertThat(response.getTrackingNumber()).isEqualTo("SHIP-050");
        assertThat(response.getStatus()).isEqualTo(ShipmentStatus.RECEIVED);
    }

    @Test
    void getByIdMissingShipmentThrowsNotFound() {
        when(shipmentRepository.findDetailedById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.getById(404L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Shipment not found: 404");
    }

    @Test
    void getAllReturnsAllForUnoptimizedRequestWithoutStatus() {
        when(shipmentRepository.findAll()).thenReturn(List.of(
                shipment(1L, "SHIP-001", ShipmentStatus.CREATED, "Minsk", "Warsaw")
        ));

        List<ShipmentResponse> responses = shipmentService.getAll(null, false);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getTrackingNumber()).isEqualTo("SHIP-001");
    }

    @Test
    void getAllReturnsFilteredForUnoptimizedRequest() {
        when(shipmentRepository.findByStatusCode(ShipmentStatus.RECEIVED.name())).thenReturn(List.of(
                shipment(2L, "SHIP-002", ShipmentStatus.RECEIVED, "Minsk", "Prague")
        ));

        List<ShipmentResponse> responses = shipmentService.getAll(ShipmentStatus.RECEIVED, false);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getStatus()).isEqualTo(ShipmentStatus.RECEIVED);
    }

    @Test
    void getAllReturnsAllForOptimizedRequestWithoutStatus() {
        when(shipmentRepository.findAllWithDetails()).thenReturn(List.of(
                shipment(3L, "SHIP-003", ShipmentStatus.CREATED, "Minsk", "Berlin")
        ));

        List<ShipmentResponse> responses = shipmentService.getAll(null, true);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getTrackingNumber()).isEqualTo("SHIP-003");
    }

    @Test
    void getAllReturnsFilteredForOptimizedRequest() {
        when(shipmentRepository.findByStatusCodeOrderByIdAsc(ShipmentStatus.RECEIVED.name())).thenReturn(List.of(
                shipment(4L, "SHIP-004", ShipmentStatus.RECEIVED, "Minsk", "Berlin")
        ));

        List<ShipmentResponse> responses = shipmentService.getAll(ShipmentStatus.RECEIVED, true);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getStatus()).isEqualTo(ShipmentStatus.RECEIVED);
    }

    @Test
    void searchReturnsCachedPageAsFromCache() {
        PageResponse<ShipmentResponse> cachedPage = new PageResponse<>(
                List.of(minimalResponse(1L, "CACHE-001", ShipmentStatus.CREATED)),
                0,
                1,
                1,
                1,
                false,
                ShipmentSearchQueryType.JPQL.name()
        );
        when(shipmentSearchIndex.get(any())).thenReturn(Optional.of(cachedPage));

        PageResponse<ShipmentResponse> response = shipmentService.search(
                " customer@test.local ",
                " paper ",
                null,
                null,
                ShipmentSearchQueryType.JPQL,
                PageRequest.of(0, 1)
        );

        assertThat(response.isFromCache()).isTrue();
        assertThat(response.getQueryType()).isEqualTo(ShipmentSearchQueryType.JPQL.name());
        assertThat(response.getContent()).hasSize(1);
        verifyNoInteractions(shipmentRepository);
    }

    @Test
    void searchBuildsAndCachesUnsortedJpqlPageWithNormalizedFilters() {
        Shipment secondShipment = shipment(2L, "SHIP-002", ShipmentStatus.RECEIVED, "Minsk", "Prague");
        Shipment firstShipment = shipment(1L, "SHIP-001", ShipmentStatus.CREATED, "Minsk", "Warsaw");
        when(shipmentSearchIndex.get(any())).thenReturn(Optional.empty());
        when(shipmentRepository.searchIdsJpql(
                "customer@test.local",
                null,
                time(1, 8),
                time(10, 18)
        )).thenReturn(List.of(2L, 1L));
        when(shipmentRepository.findAllDetailedByIdIn(List.of(2L, 1L)))
                .thenReturn(List.of(firstShipment, secondShipment));

        PageResponse<ShipmentResponse> response = shipmentService.search(
                " customer@test.local ",
                "   ",
                time(1, 8),
                time(10, 18),
                ShipmentSearchQueryType.JPQL,
                PageRequest.of(0, 2)
        );

        assertThat(response.isFromCache()).isFalse();
        assertThat(response.getQueryType()).isEqualTo(ShipmentSearchQueryType.JPQL.name());
        assertThat(response.getContent()).extracting(ShipmentResponse::getId)
                .containsExactly(2L, 1L);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(1);
        verify(shipmentSearchIndex).put(any(), any(PageResponse.class));
    }

    @Test
    void searchBuildsAndCachesUnsortedNativeEmptyPage() {
        when(shipmentSearchIndex.get(any())).thenReturn(Optional.empty());
        when(shipmentRepository.searchIdsNative(null, null, null, null)).thenReturn(List.of());

        PageResponse<ShipmentResponse> response = shipmentService.search(
                null,
                null,
                null,
                null,
                ShipmentSearchQueryType.NATIVE,
                PageRequest.of(0, 10)
        );

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isZero();
        assertThat(response.getTotalPages()).isZero();
        verify(shipmentRepository, never()).findAllDetailedByIdIn(List.of());
        verify(shipmentSearchIndex).put(any(), any(PageResponse.class));
    }

    @Test
    void searchBuildsAndCachesUnsortedPageWithZeroSize() {
        Pageable pageable = mock(Pageable.class);
        when(pageable.getPageNumber()).thenReturn(0);
        when(pageable.getPageSize()).thenReturn(0);
        when(pageable.getOffset()).thenReturn(0L);
        when(pageable.getSort()).thenReturn(Sort.unsorted());
        when(shipmentSearchIndex.get(any())).thenReturn(Optional.empty());
        when(shipmentRepository.searchIdsJpql(null, null, null, null)).thenReturn(List.of(1L));

        PageResponse<ShipmentResponse> response = shipmentService.search(
                null,
                null,
                null,
                null,
                ShipmentSearchQueryType.JPQL,
                pageable
        );

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalPages()).isZero();
        verify(shipmentRepository, never()).findAllDetailedByIdIn(List.of());
        verify(shipmentSearchIndex).put(any(), any(PageResponse.class));
    }

    @Test
    void searchBuildsSortedNativePageUsingAllSupportedComparators() {
        Shipment shipmentOne = shipment(1L, "B-100", ShipmentStatus.CREATED, "Warsaw", "Prague");
        Shipment shipmentTwo = shipment(2L, "A-100", ShipmentStatus.RECEIVED, "Berlin", "Zurich");
        Shipment shipmentThree = shipment(3L, "A-050", ShipmentStatus.RECEIVED, "Athens", "Paris");
        when(shipmentSearchIndex.get(any())).thenReturn(Optional.empty());
        when(shipmentRepository.searchIdsNative(null, "paper", null, null))
                .thenReturn(List.of(1L, 2L, 3L));
        when(shipmentRepository.findAllDetailedByIdIn(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(shipmentOne, shipmentTwo, shipmentThree));

        PageResponse<ShipmentResponse> response = shipmentService.search(
                null,
                "paper",
                null,
                null,
                ShipmentSearchQueryType.NATIVE,
                PageRequest.of(
                        0,
                        10,
                        Sort.by(
                                Sort.Order.desc("status"),
                                Sort.Order.asc("trackingNumber"),
                                Sort.Order.asc("originCity"),
                                Sort.Order.desc("destinationCity"),
                                Sort.Order.asc("id"),
                                Sort.Order.asc("unknown")
                        )
                )
        );

        assertThat(response.getContent()).extracting(ShipmentResponse::getId)
                .containsExactly(3L, 2L, 1L);
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalPages()).isEqualTo(1);
        verify(shipmentSearchIndex).put(any(), any(PageResponse.class));
    }

    @Test
    void searchBuildsSortedJpqlPageWithUnknownSortUsingOriginalOrder() {
        Shipment shipment = shipment(1L, "SHIP-UNKNOWN", ShipmentStatus.CREATED, "Minsk", "Warsaw");
        when(shipmentSearchIndex.get(any())).thenReturn(Optional.empty());
        when(shipmentRepository.searchIdsJpql(null, null, null, null)).thenReturn(List.of(1L));
        when(shipmentRepository.findAllDetailedByIdIn(List.of(1L))).thenReturn(List.of(shipment));

        PageResponse<ShipmentResponse> response = shipmentService.search(
                null,
                null,
                null,
                null,
                ShipmentSearchQueryType.JPQL,
                PageRequest.of(0, 10, Sort.by("unknown"))
        );

        assertThat(response.getContent()).extracting(ShipmentResponse::getId)
                .containsExactly(1L);
        assertThat(response.getTotalPages()).isEqualTo(1);
    }

    @Test
    void searchReturnsEmptySortedPageWhenOffsetExceedsResultSize() {
        Shipment shipmentOne = shipment(1L, "SHIP-001", ShipmentStatus.CREATED, "Minsk", "Warsaw");
        Shipment shipmentTwo = shipment(2L, "SHIP-002", ShipmentStatus.RECEIVED, "Minsk", "Prague");
        Shipment shipmentThree = shipment(3L, "SHIP-003", ShipmentStatus.DELIVERED, "Minsk", "Berlin");
        when(shipmentSearchIndex.get(any())).thenReturn(Optional.empty());
        when(shipmentRepository.searchIdsJpql(null, null, null, null)).thenReturn(List.of(1L, 2L, 3L));
        when(shipmentRepository.findAllDetailedByIdIn(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(shipmentOne, shipmentTwo, shipmentThree));

        PageResponse<ShipmentResponse> response = shipmentService.search(
                null,
                null,
                null,
                null,
                ShipmentSearchQueryType.JPQL,
                PageRequest.of(2, 2, Sort.by("id"))
        );

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalPages()).isEqualTo(2);
    }

    @Test
    void deleteExistingShipmentRemovesEntityAndInvalidatesCache() {
        when(shipmentRepository.existsById(18L)).thenReturn(true);

        shipmentService.delete(18L);

        verify(shipmentRepository).deleteById(18L);
        verify(shipmentSearchIndex).invalidateAll();
    }

    @Test
    void deleteMissingShipmentThrowsNotFound() {
        when(shipmentRepository.existsById(181L)).thenReturn(false);

        assertThatThrownBy(() -> shipmentService.delete(181L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Shipment not found: 181");

        verify(shipmentRepository, never()).deleteById(181L);
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    @Test
    void createWithPartialSaveDemoThrowsAndInvalidatesCache() {
        stubSuccessfulAggregateResolution(ShipmentStatus.CREATED);
        stubManualPersistence();

        assertThatThrownBy(() -> shipmentService.createWithPartialSaveDemo(
                buildRequest("SHIP-DEMO-1", ShipmentStatus.CREATED)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Intentional failure after partial save");

        verify(shipmentRepository).save(any(Shipment.class));
        verify(shipmentScheduleRepository).save(any(ShipmentSchedule.class));
        verify(cargoRepository).save(any(Cargo.class));
        verify(shipmentSearchIndex).invalidateAll();
    }

    @Test
    void createWithRollbackDemoThrowsAndInvalidatesCache() {
        stubSuccessfulAggregateResolution(ShipmentStatus.CREATED);
        stubManualPersistence();

        assertThatThrownBy(() -> shipmentService.createWithRollbackDemo(
                buildRequest("SHIP-DEMO-2", ShipmentStatus.CREATED)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Intentional failure after partial save");

        verify(shipmentRepository).save(any(Shipment.class));
        verify(shipmentScheduleRepository).save(any(ShipmentSchedule.class));
        verify(cargoRepository).save(any(Cargo.class));
        verify(shipmentSearchIndex).invalidateAll();
    }

    @Test
    void createBulkWithPartialSaveDemoThrowsAfterFirstSavedShipmentAndInvalidatesCache() {
        stubSuccessfulAggregateResolution(ShipmentStatus.CREATED);
        when(shipmentRepository.findByTrackingNumber(anyString())).thenReturn(Optional.empty());
        stubShipmentSaveAssignsIds();

        assertThatThrownBy(() -> shipmentService.createBulkWithPartialSaveDemo(List.of(
                buildRequest("BULK-DEMO-1", ShipmentStatus.CREATED),
                buildRequest("BULK-DEMO-2", ShipmentStatus.CREATED)
        )))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Intentional bulk failure after first saved shipment");

        verify(shipmentRepository).save(any(Shipment.class));
        verify(shipmentSearchIndex).invalidateAll();
    }

    @Test
    void createBulkWithPartialSaveDemoReturnsEmptyForEmptyInputAndInvalidatesCache() {
        List<ShipmentResponse> responses = shipmentService.createBulkWithPartialSaveDemo(List.of());

        assertThat(responses).isEmpty();
        verify(shipmentSearchIndex).invalidateAll();
    }

    @Test
    void createBulkWithRollbackDemoReturnsEmptyForEmptyInputAndInvalidatesCache() {
        List<ShipmentResponse> responses = shipmentService.createBulkWithRollbackDemo(List.of());

        assertThat(responses).isEmpty();
        verify(shipmentSearchIndex).invalidateAll();
    }

    @Test
    void persistShipmentWithFirstCargoSavesMinimalAggregate() {
        stubSuccessfulAggregateResolution(ShipmentStatus.RECEIVED);
        stubManualPersistence();
        ShipmentRequest request = buildRequest(
                "SHIP-MANUAL",
                ShipmentStatus.RECEIVED,
                List.of(
                        cargo("Paper", "100.00"),
                        cargo("Glass", "200.00")
                ),
                schedule(8),
                List.of(vehicle.getId()),
                customer.getId(),
                manager.getId()
        );

        Shipment persistedShipment = ReflectionTestUtils.invokeMethod(
                shipmentService,
                "persistShipmentWithFirstCargo",
                request
        );

        assertThat(persistedShipment.getTrackingNumber()).isEqualTo("SHIP-MANUAL");
        assertThat(persistedShipment.getStatus().getCode()).isEqualTo(ShipmentStatus.RECEIVED.name());
        assertThat(persistedShipment.getCargoes()).hasSize(1);
        assertThat(persistedShipment.getCargoes().getFirst().getName()).isEqualTo("Paper");
        assertThat(persistedShipment.getSchedule()).isNotNull();
        verify(cargoRepository).save(any(Cargo.class));
        verify(shipmentSearchIndex, never()).invalidateAll();
    }

    @Test
    void shipmentResponseComparatorHandlesNullStatusValues() {
        Comparator<ShipmentResponse> comparator = ReflectionTestUtils.invokeMethod(
                shipmentService,
                "shipmentResponseComparator",
                Sort.Order.asc("status")
        );

        int comparison = comparator.compare(
                minimalResponse(1L, "SHIP-NULL", null),
                minimalResponse(2L, "SHIP-CREATED", ShipmentStatus.CREATED)
        );

        assertThat(comparison).isGreaterThan(0);
    }

    private void stubSuccessfulAggregateResolution(ShipmentStatus... statuses) {
        stubUsers();
        when(vehicleRepository.findAllById(List.of(vehicle.getId()))).thenReturn(List.of(vehicle));
        for (ShipmentStatus status : statuses) {
            when(shipmentStatusLookupRepository.findByCode(status.name()))
                    .thenReturn(Optional.of(statusLookup(status)));
        }
    }

    private void stubUsers() {
        when(appUserRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(appUserRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
    }

    private void stubShipmentSaveAssignsIds() {
        AtomicLong sequence = new AtomicLong(1L);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
            Shipment shipment = invocation.getArgument(0);
            if (shipment.getId() == null) {
                shipment.setId(sequence.getAndIncrement());
            }
            return shipment;
        });
    }

    private void stubManualPersistence() {
        stubShipmentSaveAssignsIds();
        AtomicLong cargoSequence = new AtomicLong(100L);
        when(shipmentScheduleRepository.save(any(ShipmentSchedule.class))).thenAnswer(invocation -> {
            ShipmentSchedule schedule = invocation.getArgument(0);
            if (schedule.getId() == null) {
                schedule.setId(50L);
            }
            return schedule;
        });
        when(cargoRepository.save(any(Cargo.class))).thenAnswer(invocation -> {
            Cargo cargo = invocation.getArgument(0);
            if (cargo.getId() == null) {
                cargo.setId(cargoSequence.getAndIncrement());
            }
            return cargo;
        });
    }

    private ShipmentRequest buildRequest(String trackingNumber, ShipmentStatus status) {
        return buildRequest(
                trackingNumber,
                status,
                List.of(cargo("Boxes", "150.00")),
                schedule(3),
                List.of(vehicle.getId()),
                customer.getId(),
                manager.getId()
        );
    }

    private ShipmentRequest buildRequest(
            String trackingNumber,
            ShipmentStatus status,
            List<CargoRequest> cargoes,
            ShipmentScheduleRequest schedule,
            List<Long> vehicleIds,
            Long customerId,
            Long managerId
    ) {
        return new ShipmentRequest(
                trackingNumber,
                "Minsk",
                "Warsaw",
                status,
                customerId,
                managerId,
                vehicleIds,
                cargoes,
                schedule
        );
    }

    private Shipment shipment(
            Long id,
            String trackingNumber,
            ShipmentStatus status,
            String originCity,
            String destinationCity
    ) {
        Shipment shipment = new Shipment();
        shipment.setId(id);
        shipment.setTrackingNumber(trackingNumber);
        shipment.setOriginCity(originCity);
        shipment.setDestinationCity(destinationCity);
        shipment.setStatus(statusLookup(status));
        shipment.setCustomer(customer);
        shipment.setManager(manager);
        shipment.setVehicles(new LinkedHashSet<>(List.of(vehicle)));
        shipment.addCargo(new Cargo(id + 100, "Cargo-" + trackingNumber, decimal("10.00"), null));
        shipment.setSchedule(new ShipmentSchedule(
                id + 200,
                time(1, 10),
                time(1, 12),
                time(3, 18),
                null
        ));
        return shipment;
    }

    private ShipmentResponse minimalResponse(Long id, String trackingNumber, ShipmentStatus status) {
        return new ShipmentResponse(
                id,
                trackingNumber,
                "Minsk",
                "Warsaw",
                status,
                null,
                null,
                List.of(),
                null,
                List.of()
        );
    }

    private AppUser user(Long id, String firstName, UserRole role) {
        return new AppUser(
                id,
                firstName,
                role.name(),
                role.name().toLowerCase() + id + "@test.local",
                new UserRoleLookup(id, role.name()),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private ShipmentStatusLookup statusLookup(ShipmentStatus status) {
        return new ShipmentStatusLookup((long) (status.ordinal() + 1), status.name());
    }

    private CargoRequest cargo(String name, String weightKg) {
        return new CargoRequest(name, decimal(weightKg));
    }

    private ShipmentScheduleRequest schedule(int arrivalDay) {
        return new ShipmentScheduleRequest(
                time(1, 10),
                time(1, 12),
                time(arrivalDay, 18)
        );
    }

    private LocalDateTime time(int day, int hour) {
        return LocalDateTime.of(2026, 4, day, hour, 0);
    }

    private BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }
}
