package com.logisticsapplication;

import com.logisticsapplication.model.AppUser;
import com.logisticsapplication.model.Shipment;
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
import com.logisticsapplication.repository.UserRoleLookupRepository;
import com.logisticsapplication.repository.VehicleRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiEndpointsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private ShipmentScheduleRepository shipmentScheduleRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserRoleLookupRepository userRoleLookupRepository;

    @Autowired
    private ShipmentStatusLookupRepository shipmentStatusLookupRepository;

    private AppUser customer;
    private AppUser manager;
    private AppUser carrier;
    private Vehicle vehicle;
    private Shipment shipment;
    private String authToken;

    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        cargoRepository.deleteAll();
        shipmentScheduleRepository.deleteAll();
        shipmentRepository.deleteAll();
        vehicleRepository.deleteAll();
        appUserRepository.deleteAll();

        customer = appUserRepository.save(new AppUser(
                null,
                "Vlad",
                "Mogila",
                "mogila@test.local",
                "mogila@test.local",
                TEST_PASSWORD,
                getRole(UserRole.CUSTOMER),
                null,
                null,
                null
        ));
        manager = appUserRepository.save(new AppUser(
                null,
                "Maksim",
                "Efimchik",
                "efimchik@test.local",
                "efimchik@test.local",
                TEST_PASSWORD,
                getRole(UserRole.MANAGER),
                null,
                null,
                null
        ));
        carrier = appUserRepository.save(new AppUser(
                null,
                "Evgeniy",
                "Apanas",
                "apanas@test.local",
                "apanas@test.local",
                TEST_PASSWORD,
                getRole(UserRole.CARRIER),
                null,
                null,
                null
        ));

        vehicle = vehicleRepository.save(new Vehicle(
                null,
                "EF-9011",
                new BigDecimal("7000.00"),
                carrier,
                null
        ));

        ShipmentStatusLookup inTransit = getStatus(ShipmentStatus.IN_TRANSIT);
        shipment = new Shipment();
        shipment.setTrackingNumber("SHIP-6001");
        shipment.setOriginCity("Minsk");
        shipment.setDestinationCity("Prague");
        shipment.setStatus(inTransit);
        shipment.setCustomer(customer);
        shipment.setManager(manager);
        shipment.setVehicles(new java.util.LinkedHashSet<>(List.of(vehicle)));
        shipment.addCargo(new com.logisticsapplication.model.Cargo(
                null,
                "Electronics",
                new BigDecimal("1200.50"),
                shipment
        ));
        shipment.setSchedule(new com.logisticsapplication.model.ShipmentSchedule(
                null,
                LocalDateTime.of(2026, 3, 11, 10, 0),
                LocalDateTime.of(2026, 3, 11, 12, 0),
                LocalDateTime.of(2026, 3, 14, 9, 0),
                shipment
        ));
        shipment = shipmentRepository.save(shipment);
        authToken = authenticate("efimchik@test.local", TEST_PASSWORD);
    }

    @Test
    void healthEndpointWorks() throws Exception {
        mockMvc.perform(authorized(get("/api/health")))
                .andExpect(status().isOk());
    }

    @Test
    void userEndpointsWork() throws Exception {
        String createBody = """
                {
                  "firstName": "Temp",
                  "lastName": "Manager",
                  "email": "temp.manager@test.local",
                  "login": "temp.manager@test.local",
                  "password": "password123",
                  "role": "MANAGER"
                }
                """;

        mockMvc.perform(authorized(get("/api/users")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());

        mockMvc.perform(authorized(get("/api/users/{id}", manager.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("efimchik@test.local"));

        String createResponse = mockMvc.perform(
                        authorized(post("/api/users"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("temp.manager@test.local"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long createdId = extractId(createResponse);
        String updateBody = """
                {
                  "firstName": "Updated",
                  "lastName": "Manager",
                  "email": "updated.manager@test.local",
                  "login": "updated.manager@test.local",
                  "password": "password123",
                  "role": "MANAGER"
                }
                """;

        mockMvc.perform(
                        authorized(put("/api/users/{id}", createdId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated.manager@test.local"));

        mockMvc.perform(authorized(delete("/api/users/{id}", createdId)))
                .andExpect(status().isNoContent());
    }

    @Test
    void vehicleEndpointsWork() throws Exception {
        String createBody = """
                {
                  "registrationNumber": "TEMP-9001",
                  "capacityKg": 4500.00,
                  "carrierId": %d
                }
                """.formatted(carrier.getId());

        mockMvc.perform(authorized(get("/api/vehicles")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].registrationNumber").exists());

        mockMvc.perform(authorized(get("/api/vehicles/{id}", vehicle.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("EF-9011"));

        String createResponse = mockMvc.perform(
                        authorized(post("/api/vehicles"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("TEMP-9001"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long createdId = extractId(createResponse);
        String updateBody = """
                {
                  "registrationNumber": "TEMP-9002",
                  "capacityKg": 4800.00,
                  "carrierId": %d
                }
                """.formatted(carrier.getId());

        mockMvc.perform(
                        authorized(put("/api/vehicles/{id}", createdId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("TEMP-9002"));

        mockMvc.perform(authorized(delete("/api/vehicles/{id}", createdId)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shipmentEndpointsAndSearchWork() throws Exception {
        String createBody = """
                {
                  "trackingNumber": "TEMP-SHIP-9001",
                  "originCity": "Minsk",
                  "destinationCity": "Warsaw",
                  "status": "CREATED",
                  "customerId": %d,
                  "managerId": %d,
                  "vehicleIds": [%d],
                  "cargoes": [
                    {
                      "name": "Monitors",
                      "weightKg": 300.00
                    }
                  ],
                  "schedule": {
                    "orderCreatedAt": "2026-03-24T10:00:00",
                    "orderReceivedAt": "2026-03-24T12:00:00",
                    "arrivalAt": "2026-03-27T14:00:00"
                  }
                }
                """.formatted(customer.getId(), manager.getId(), vehicle.getId());

        mockMvc.perform(authorized(get("/api/shipments")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trackingNumber").value("SHIP-6001"));

        mockMvc.perform(authorized(get("/api/shipments").param("optimized", "true")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trackingNumber").value("SHIP-6001"));

        mockMvc.perform(authorized(get("/api/shipments/{id}", shipment.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingNumber").value("SHIP-6001"));

        String createResponse = mockMvc.perform(
                        authorized(post("/api/shipments"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingNumber").value("TEMP-SHIP-9001"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(
                        authorized(get("/api/shipments/search"))
                                .param("customerEmail", "mogila@test.local")
                                .param("cargoName", "Electronics")
                                .param("arrivalTo", "2026-03-15T23:59:59")
                                .param("queryType", "JPQL")
                                .param("page", "0")
                                .param("size", "5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCache").value(false))
                .andExpect(jsonPath("$.queryType").value("JPQL"))
                .andExpect(jsonPath("$.content[0].trackingNumber").value("SHIP-6001"));

        mockMvc.perform(
                        authorized(get("/api/shipments/search"))
                                .param("customerEmail", "mogila@test.local")
                                .param("cargoName", "Electronics")
                                .param("arrivalTo", "2026-03-15T23:59:59")
                                .param("queryType", "JPQL")
                                .param("page", "0")
                                .param("size", "5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCache").value(true));

        mockMvc.perform(
                        authorized(get("/api/shipments/search"))
                                .param("customerEmail", "mogila@test.local")
                                .param("cargoName", "Electronics")
                                .param("arrivalTo", "2026-03-15T23:59:59")
                                .param("queryType", "NATIVE")
                                .param("page", "0")
                                .param("size", "5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryType").value("NATIVE"))
                .andExpect(jsonPath("$.content[0].trackingNumber").value("SHIP-6001"));

        Long createdId = extractId(createResponse);
        String updateBody = """
                {
                  "trackingNumber": "TEMP-SHIP-9001",
                  "originCity": "Minsk",
                  "destinationCity": "Budapest",
                  "status": "RECEIVED",
                  "customerId": %d,
                  "managerId": %d,
                  "vehicleIds": [%d],
                  "cargoes": [
                    {
                      "name": "Monitors Updated",
                      "weightKg": 320.00
                    }
                  ],
                  "schedule": {
                    "orderCreatedAt": "2026-03-24T10:00:00",
                    "orderReceivedAt": "2026-03-24T13:30:00",
                    "arrivalAt": "2026-03-28T16:00:00"
                  }
                }
                """.formatted(customer.getId(), manager.getId(), vehicle.getId());

        mockMvc.perform(
                        authorized(put("/api/shipments/{id}", createdId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destinationCity").value("Budapest"));

        mockMvc.perform(authorized(delete("/api/shipments/{id}", createdId)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shipmentBulkEndpointWorks() throws Exception {
        String bulkBody = """
                [
                  {
                    "trackingNumber": "TEMP-SHIP-BULK-9001",
                    "originCity": "Minsk",
                    "destinationCity": "Warsaw",
                    "status": "CREATED",
                    "customerId": %d,
                    "managerId": %d,
                    "vehicleIds": [%d],
                    "cargoes": [
                      {
                        "name": "Bulk Cargo 1",
                        "weightKg": 110.00
                      }
                    ],
                    "schedule": {
                      "orderCreatedAt": "2026-03-24T10:00:00",
                      "orderReceivedAt": "2026-03-24T12:00:00",
                      "arrivalAt": "2026-03-27T14:00:00"
                    }
                  },
                  {
                    "trackingNumber": "TEMP-SHIP-BULK-9002",
                    "originCity": "Minsk",
                    "destinationCity": "Vilnius",
                    "status": "RECEIVED",
                    "customerId": %d,
                    "managerId": %d,
                    "vehicleIds": [%d],
                    "cargoes": [
                      {
                        "name": "Bulk Cargo 2",
                        "weightKg": 210.00
                      }
                    ],
                    "schedule": {
                      "orderCreatedAt": "2026-03-25T09:00:00",
                      "orderReceivedAt": "2026-03-25T10:00:00",
                      "arrivalAt": "2026-03-28T16:00:00"
                    }
                  }
                ]
                """.formatted(
                customer.getId(),
                manager.getId(),
                vehicle.getId(),
                customer.getId(),
                manager.getId(),
                vehicle.getId()
        );

        mockMvc.perform(
                        authorized(post("/api/shipments/bulk"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bulkBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trackingNumber").value("TEMP-SHIP-BULK-9001"))
                .andExpect(jsonPath("$[1].trackingNumber").value("TEMP-SHIP-BULK-9002"));
    }

    @Test
    void demoEndpointsReturnIntentionalServerError() throws Exception {
        String body = """
                {
                  "trackingNumber": "DEMO-%s",
                  "originCity": "Minsk",
                  "destinationCity": "Berlin",
                  "status": "CREATED",
                  "customerId": %d,
                  "managerId": %d,
                  "vehicleIds": [%d],
                  "cargoes": [
                    {
                      "name": "Paper",
                      "weightKg": 150.00
                    },
                    {
                      "name": "Boxes",
                      "weightKg": 200.00
                    }
                  ],
                  "schedule": {
                    "orderCreatedAt": "2026-03-24T09:00:00",
                    "orderReceivedAt": "2026-03-24T11:00:00",
                    "arrivalAt": "2026-03-25T18:00:00"
                  }
                }
                """.formatted(
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()),
                customer.getId(),
                manager.getId(),
                vehicle.getId()
        );

        mockMvc.perform(
                        authorized(post("/api/shipments/demo/partial-save"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Intentional failure after partial save"));

        mockMvc.perform(
                        authorized(post("/api/shipments/demo/rollback"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body.replace("DEMO-", "ROLL-"))
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Intentional failure after partial save"));
    }

    @Test
    void shipmentCreationFailsWhenCargoWeightExceedsThirtyTons() throws Exception {
        String body = """
                {
                  "trackingNumber": "TEMP-SHIP-OVERWEIGHT",
                  "originCity": "Minsk",
                  "destinationCity": "Warsaw",
                  "status": "CREATED",
                  "customerId": %d,
                  "managerId": %d,
                  "vehicleIds": [%d],
                  "cargoes": [
                    {
                      "name": "Heavy Machinery",
                      "weightKg": 30000.01
                    }
                  ],
                  "schedule": {
                    "orderCreatedAt": "2026-03-24T10:00:00",
                    "orderReceivedAt": "2026-03-24T12:00:00",
                    "arrivalAt": "2026-03-27T14:00:00"
                  }
                }
                """.formatted(customer.getId(), manager.getId(), vehicle.getId());

        mockMvc.perform(
                        authorized(post("/api/shipments"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['cargoes[0].weightKg']")
                        .value("weightKg must be less than or equal to 30000"));
    }

    @Test
    void shipmentCreationFailsWhenScheduleDatesOrderIsInvalid() throws Exception {
        String body = """
                {
                  "trackingNumber": "TEMP-SHIP-BAD-DATES",
                  "originCity": "Minsk",
                  "destinationCity": "Warsaw",
                  "status": "CREATED",
                  "customerId": %d,
                  "managerId": %d,
                  "vehicleIds": [%d],
                  "cargoes": [
                    {
                      "name": "Documents",
                      "weightKg": 20.00
                    }
                  ],
                  "schedule": {
                    "orderCreatedAt": "2026-03-24T10:00:00",
                    "orderReceivedAt": "2020-03-24T12:00:00",
                    "arrivalAt": "2026-03-27T14:00:00"
                  }
                }
                """.formatted(customer.getId(), manager.getId(), vehicle.getId());

        mockMvc.perform(
                        authorized(post("/api/shipments"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['schedule.orderReceivedAtValid']")
                        .value("orderReceivedAt must be equal to or after orderCreatedAt"));
    }

    private UserRoleLookup getRole(UserRole role) {
        return userRoleLookupRepository.findByCode(role.name()).orElseThrow();
    }

    private ShipmentStatusLookup getStatus(ShipmentStatus status) {
        return shipmentStatusLookupRepository.findByCode(status.name()).orElseThrow();
    }

    private Long extractId(String response) {
        int idFieldStart = response.indexOf("\"id\":");
        int valueStart = response.indexOf(':', idFieldStart) + 1;
        int valueEnd = response.indexOf(',', valueStart);
        if (valueEnd == -1) {
            valueEnd = response.indexOf('}', valueStart);
        }
        return Long.parseLong(response.substring(valueStart, valueEnd).trim());
    }

    private MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder requestBuilder) {
        return requestBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
    }

    private String authenticate(String login, String password) {
        String loginBody = """
                {
                  "login": "%s",
                  "password": "%s"
                }
                """.formatted(login, password);
        try {
            String response = mockMvc.perform(
                            post("/api/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(loginBody)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isString())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            return extractStringField(response, "token");
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to authenticate test user", exception);
        }
    }

    private String extractStringField(String response, String fieldName) {
        String fieldToken = "\"" + fieldName + "\":\"";
        int valueStart = response.indexOf(fieldToken);
        if (valueStart < 0) {
            throw new IllegalArgumentException("Field not found in response: " + fieldName);
        }
        valueStart += fieldToken.length();
        int valueEnd = response.indexOf('"', valueStart);
        return response.substring(valueStart, valueEnd);
    }
}
