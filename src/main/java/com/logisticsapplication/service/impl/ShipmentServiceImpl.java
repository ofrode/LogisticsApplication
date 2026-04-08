package com.logisticsapplication.service.impl;

import com.logisticsapplication.cache.ShipmentSearchCacheKey;
import com.logisticsapplication.cache.ShipmentSearchIndex;
import com.logisticsapplication.dto.request.CargoRequest;
import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.request.ShipmentScheduleRequest;
import com.logisticsapplication.dto.response.PageResponse;
import com.logisticsapplication.dto.response.ShipmentResponse;
import com.logisticsapplication.mapper.ShipmentMapper;
import com.logisticsapplication.model.AppUser;
import com.logisticsapplication.model.Cargo;
import com.logisticsapplication.model.Shipment;
import com.logisticsapplication.model.ShipmentSchedule;
import com.logisticsapplication.model.ShipmentSearchQueryType;
import com.logisticsapplication.model.ShipmentStatus;
import com.logisticsapplication.model.ShipmentStatusLookup;
import com.logisticsapplication.model.UserRole;
import com.logisticsapplication.model.Vehicle;
import com.logisticsapplication.repository.AppUserRepository;
import com.logisticsapplication.repository.CargoRepository;
import com.logisticsapplication.repository.ShipmentRepository;
import com.logisticsapplication.repository.ShipmentScheduleRepository;
import com.logisticsapplication.repository.ShipmentStatusLookupRepository;
import com.logisticsapplication.repository.VehicleRepository;
import com.logisticsapplication.service.ShipmentService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private static final String PARTIAL_SAVE_DEMO_FAILURE_MESSAGE =
            "Intentional failure after partial save";
    private static final String BULK_DEMO_FAILURE_MESSAGE =
            "Intentional bulk failure after first saved shipment";

    private final ShipmentRepository shipmentRepository;
    private final AppUserRepository appUserRepository;
    private final VehicleRepository vehicleRepository;
    private final ShipmentScheduleRepository shipmentScheduleRepository;
    private final CargoRepository cargoRepository;
    private final ShipmentStatusLookupRepository shipmentStatusLookupRepository;
    private final ShipmentSearchIndex shipmentSearchIndex;

    @Override
    @Transactional
    public ShipmentResponse create(ShipmentRequest request) {
        validateTrackingNumberAvailability(request.getTrackingNumber(), null);
        ShipmentResponse response = saveNewShipment(request);
        shipmentSearchIndex.invalidateAll();
        return response;
    }

    @Override
    @Transactional
    public List<ShipmentResponse> createBulk(List<ShipmentRequest> requests) {
        validateBulkRequests(requests);
        List<ShipmentResponse> responses = requests.stream()
                .map(this::saveNewShipment)
                .toList();
        shipmentSearchIndex.invalidateAll();
        return responses;
    }

    @Override
    @Transactional
    public ShipmentResponse update(Long id, ShipmentRequest request) {
        Shipment shipment = shipmentRepository.findDetailedById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found: " + id)
        );
        validateTrackingNumberAvailability(request.getTrackingNumber(), id);
        applyAggregate(shipment, request);
        ShipmentResponse response = ShipmentMapper.toResponse(shipmentRepository.save(shipment));
        shipmentSearchIndex.invalidateAll();
        return response;
    }

    @Override
    @Transactional
    public ShipmentResponse getById(Long id) {
        Shipment shipment = shipmentRepository.findDetailedById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found: " + id)
        );
        return ShipmentMapper.toResponse(shipment);
    }

    @Override
    @Transactional
    public List<ShipmentResponse> getAll(ShipmentStatus status, boolean optimized) {
        List<Shipment> shipments;
        if (optimized) {
            shipments = status == null
                    ? shipmentRepository.findAllWithDetails()
                    : shipmentRepository.findByStatusCodeOrderByIdAsc(status.name());
        } else {
            shipments = status == null
                    ? shipmentRepository.findAll()
                    : shipmentRepository.findByStatusCode(status.name());
        }
        return shipments.stream()
                .map(ShipmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PageResponse<ShipmentResponse> search(
            String customerEmail,
            String cargoName,
            LocalDateTime arrivalFrom,
            LocalDateTime arrivalTo,
            ShipmentSearchQueryType queryType,
            Pageable pageable
    ) {
        String normalizedCustomerEmail = normalize(customerEmail);
        String normalizedCargoName = normalize(cargoName);
        ShipmentSearchCacheKey cacheKey = new ShipmentSearchCacheKey(
                new ShipmentSearchCacheKey.SearchCriteria(
                        normalizedCustomerEmail,
                        normalizedCargoName,
                        arrivalFrom,
                        arrivalTo,
                        queryType.name()
                ),
                new ShipmentSearchCacheKey.PageDescriptor(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSort().toString()
                )
        );

        return shipmentSearchIndex.get(cacheKey)
                .map(this::copyCachedPage)
                .orElseGet(
                        () -> buildAndCacheSearchResponse(
                                normalizedCustomerEmail,
                                normalizedCargoName,
                                arrivalFrom,
                                arrivalTo,
                                queryType,
                                pageable,
                                cacheKey
                        )
                );
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!shipmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found: " + id);
        }
        shipmentRepository.deleteById(id);
        shipmentSearchIndex.invalidateAll();
    }

    @Override
    public ShipmentResponse createWithPartialSaveDemo(ShipmentRequest request) {
        throw buildManualFailure(request);
    }

    @Override
    @Transactional
    public ShipmentResponse createWithRollbackDemo(ShipmentRequest request) {
        throw buildManualFailure(request);
    }

    @Override
    public List<ShipmentResponse> createBulkWithPartialSaveDemo(List<ShipmentRequest> requests) {
        try {
            return saveBulkWithIntentionalFailure(requests);
        } finally {
            shipmentSearchIndex.invalidateAll();
        }
    }

    @Override
    @Transactional
    public List<ShipmentResponse> createBulkWithRollbackDemo(List<ShipmentRequest> requests) {
        try {
            return saveBulkWithIntentionalFailure(requests);
        } finally {
            shipmentSearchIndex.invalidateAll();
        }
    }

    private ShipmentResponse saveNewShipment(ShipmentRequest request) {
        return ShipmentMapper.toResponse(saveNewShipmentEntity(request));
    }

    private Shipment saveNewShipmentEntity(ShipmentRequest request) {
        Shipment shipment = new Shipment();
        applyAggregate(shipment, request);
        return shipmentRepository.save(shipment);
    }

    private List<ShipmentResponse> saveBulkWithIntentionalFailure(List<ShipmentRequest> requests) {
        validateBulkRequests(requests);
        if (requests.isEmpty()) {
            return List.of();
        }
        saveNewShipmentEntity(requests.getFirst());
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                BULK_DEMO_FAILURE_MESSAGE
        );
    }

    private ResponseStatusException buildManualFailure(ShipmentRequest request) {
        persistShipmentWithFirstCargo(request);
        shipmentSearchIndex.invalidateAll();
        return new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                PARTIAL_SAVE_DEMO_FAILURE_MESSAGE
        );
    }

    private Shipment persistShipmentWithFirstCargo(ShipmentRequest request) {
        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(request.getTrackingNumber());
        shipment.setOriginCity(request.getOriginCity());
        shipment.setDestinationCity(request.getDestinationCity());
        shipment.setStatus(getShipmentStatus(request.getStatus()));
        shipment.setCustomer(
                getUserByRole(request.getCustomerId(), UserRole.CUSTOMER, "Customer")
        );
        shipment.setManager(
                getUserByRole(request.getManagerId(), UserRole.MANAGER, "Manager")
        );
        shipment.setVehicles(resolveVehicles(request.getVehicleIds()));
        Shipment persistedShipment = shipmentRepository.save(shipment);

        ShipmentSchedule schedule = buildSchedule(request.getSchedule());
        schedule.setShipment(persistedShipment);
        shipmentScheduleRepository.save(schedule);
        persistedShipment.setSchedule(schedule);

        CargoRequest firstCargoRequest = request.getCargoes().getFirst();
        Cargo firstCargo = new Cargo(
                null,
                firstCargoRequest.getName(),
                firstCargoRequest.getWeightKg(),
                persistedShipment
        );
        cargoRepository.save(firstCargo);
        persistedShipment.getCargoes().add(firstCargo);
        return persistedShipment;
    }

    private void validateBulkRequests(List<ShipmentRequest> requests) {
        List<String> duplicateTrackingNumbers = requests.stream()
                .map(ShipmentRequest::getTrackingNumber)
                .collect(
                        Collectors.groupingBy(
                                trackingNumber -> trackingNumber,
                                Collectors.counting()
                        )
                )
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
        if (!duplicateTrackingNumbers.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Bulk request contains duplicate tracking numbers: "
                            + duplicateTrackingNumbers
            );
        }
        requests.stream()
                .map(ShipmentRequest::getTrackingNumber)
                .forEach(
                        trackingNumber -> validateTrackingNumberAvailability(
                                trackingNumber,
                                null
                        )
                );
    }

    private void validateTrackingNumberAvailability(String trackingNumber, Long currentShipmentId) {
        shipmentRepository.findByTrackingNumber(trackingNumber)
                .filter(existingShipment -> currentShipmentId == null
                        || !existingShipment.getId().equals(currentShipmentId))
                .ifPresent(existingShipment -> {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Tracking number already exists: " + trackingNumber
                    );
                });
    }

    private void applyAggregate(Shipment shipment, ShipmentRequest request) {
        shipment.setTrackingNumber(request.getTrackingNumber());
        shipment.setOriginCity(request.getOriginCity());
        shipment.setDestinationCity(request.getDestinationCity());
        shipment.setStatus(getShipmentStatus(request.getStatus()));
        shipment.setCustomer(
                getUserByRole(request.getCustomerId(), UserRole.CUSTOMER, "Customer")
        );
        shipment.setManager(
                getUserByRole(request.getManagerId(), UserRole.MANAGER, "Manager")
        );
        shipment.setVehicles(resolveVehicles(request.getVehicleIds()));
        shipment.clearCargoes();
        request.getCargoes().forEach(
                cargoRequest -> shipment.addCargo(buildCargo(cargoRequest))
        );
        applySchedule(shipment, request.getSchedule());
    }

    private Cargo buildCargo(CargoRequest request) {
        return new Cargo(null, request.getName(), request.getWeightKg(), null);
    }

    private ShipmentSchedule buildSchedule(ShipmentScheduleRequest request) {
        return new ShipmentSchedule(
                null,
                request.getOrderCreatedAt(),
                request.getOrderReceivedAt(),
                request.getArrivalAt(),
                null
        );
    }

    private void applySchedule(Shipment shipment, ShipmentScheduleRequest request) {
        ShipmentSchedule schedule = shipment.getSchedule();
        if (schedule == null) {
            shipment.setSchedule(buildSchedule(request));
            return;
        }
        schedule.setOrderCreatedAt(request.getOrderCreatedAt());
        schedule.setOrderReceivedAt(request.getOrderReceivedAt());
        schedule.setArrivalAt(request.getArrivalAt());
    }

    private Set<Vehicle> resolveVehicles(List<Long> vehicleIds) {
        Set<Vehicle> vehicles = new LinkedHashSet<>(vehicleRepository.findAllById(vehicleIds));
        if (vehicles.size() != vehicleIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Some vehicles were not found"
            );
        }
        return vehicles;
    }

    private AppUser getUserByRole(Long id, UserRole role, String label) {
        AppUser user = appUserRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, label + " not found: " + id)
        );
        if (!role.name().equals(user.getRole().getCode())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    label + " must have role " + role
            );
        }
        return user;
    }

    private ShipmentStatusLookup getShipmentStatus(ShipmentStatus status) {
        return shipmentStatusLookupRepository.findByCode(status.name()).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Shipment status lookup not found: " + status.name()
                )
        );
    }

    private List<ShipmentResponse> buildOrderedResponses(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, ShipmentResponse> responsesById = new HashMap<>();
        shipmentRepository.findAllDetailedByIdIn(ids).forEach(
                shipment -> responsesById.put(shipment.getId(), ShipmentMapper.toResponse(shipment))
        );
        return ids.stream()
                .map(responsesById::get)
                .toList();
    }

    private SearchPage findUnsortedPage(
            String customerEmail,
            String cargoName,
            LocalDateTime arrivalFrom,
            LocalDateTime arrivalTo,
            ShipmentSearchQueryType queryType,
            Pageable pageable
    ) {
        List<Long> allIds = queryType == ShipmentSearchQueryType.NATIVE
                ? shipmentRepository.searchIdsNative(
                        customerEmail,
                        cargoName,
                        arrivalFrom,
                        arrivalTo
                )
                : shipmentRepository.searchIdsJpql(
                        customerEmail,
                        cargoName,
                        arrivalFrom,
                        arrivalTo
                );
        return buildPageFromIds(allIds, pageable);
    }

    private SearchPage findSortedPage(
            String customerEmail,
            String cargoName,
            LocalDateTime arrivalFrom,
            LocalDateTime arrivalTo,
            ShipmentSearchQueryType queryType,
            Pageable pageable
    ) {
        List<Long> allIds = queryType == ShipmentSearchQueryType.NATIVE
                ? shipmentRepository.searchIdsNative(
                        customerEmail,
                        cargoName,
                        arrivalFrom,
                        arrivalTo
                )
                : shipmentRepository.searchIdsJpql(
                        customerEmail,
                        cargoName,
                        arrivalFrom,
                        arrivalTo
                );
        List<ShipmentResponse> sortedResponses = sortResponses(
                buildOrderedResponses(allIds),
                pageable.getSort()
        );
        int fromIndex = Math.toIntExact(pageable.getOffset());
        if (fromIndex >= sortedResponses.size()) {
            return new SearchPage(List.of(), sortedResponses.size());
        }
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), sortedResponses.size());
        return new SearchPage(
                sortedResponses.subList(fromIndex, toIndex),
                sortedResponses.size()
        );
    }

    private SearchPage buildPageFromIds(List<Long> allIds, Pageable pageable) {
        int fromIndex = Math.toIntExact(pageable.getOffset());
        if (fromIndex >= allIds.size()) {
            return new SearchPage(List.of(), allIds.size());
        }
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), allIds.size());
        return new SearchPage(
                buildOrderedResponses(allIds.subList(fromIndex, toIndex)),
                allIds.size()
        );
    }

    private List<ShipmentResponse> sortResponses(List<ShipmentResponse> responses, Sort sort) {
        Comparator<ShipmentResponse> comparator = null;
        for (Sort.Order order : sort) {
            Comparator<ShipmentResponse> nextComparator = shipmentResponseComparator(order);
            if (nextComparator == null) {
                continue;
            }
            comparator = comparator == null
                    ? nextComparator
                    : comparator.thenComparing(nextComparator);
        }
        if (comparator == null) {
            return responses;
        }
        return responses.stream()
                .sorted(comparator)
                .toList();
    }

    private Comparator<ShipmentResponse> shipmentResponseComparator(Sort.Order order) {
        Comparator<ShipmentResponse> comparator = switch (order.getProperty()) {
            case "id" -> Comparator.comparing(ShipmentResponse::getId);
            case "trackingNumber" -> Comparator.comparing(
                    ShipmentResponse::getTrackingNumber,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );
            case "originCity" -> Comparator.comparing(
                    ShipmentResponse::getOriginCity,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );
            case "destinationCity" -> Comparator.comparing(
                    ShipmentResponse::getDestinationCity,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );
            case "status" -> Comparator.comparing(
                    response -> response.getStatus() == null
                            ? null
                            : response.getStatus().name(),
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );
            default -> null;
        };
        if (comparator == null) {
            return null;
        }
        return order.isAscending() ? comparator : comparator.reversed();
    }

    private PageResponse<ShipmentResponse> copyCachedPage(PageResponse<ShipmentResponse> cached) {
        return new PageResponse<>(
                cached.getContent(),
                cached.getPage(),
                cached.getSize(),
                cached.getTotalElements(),
                cached.getTotalPages(),
                true,
                cached.getQueryType()
        );
    }

    private PageResponse<ShipmentResponse> buildAndCacheSearchResponse(
            String customerEmail,
            String cargoName,
            LocalDateTime arrivalFrom,
            LocalDateTime arrivalTo,
            ShipmentSearchQueryType queryType,
            Pageable pageable,
            ShipmentSearchCacheKey cacheKey
    ) {
        SearchPage searchPage = pageable.getSort().isSorted()
                ? findSortedPage(
                        customerEmail,
                        cargoName,
                        arrivalFrom,
                        arrivalTo,
                        queryType,
                        pageable
                )
                : findUnsortedPage(
                        customerEmail,
                        cargoName,
                        arrivalFrom,
                        arrivalTo,
                        queryType,
                        pageable
                );

        PageResponse<ShipmentResponse> response = new PageResponse<>(
                searchPage.content(),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                searchPage.totalElements(),
                calculateTotalPages(searchPage.totalElements(), pageable.getPageSize()),
                false,
                queryType.name()
        );
        shipmentSearchIndex.put(cacheKey, response);
        return response;
    }

    private int calculateTotalPages(long totalElements, int pageSize) {
        if (pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    private record SearchPage(List<ShipmentResponse> content, long totalElements) {
    }

    private String normalize(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(trimmed -> !trimmed.isEmpty())
                .orElse(null);
    }
}
