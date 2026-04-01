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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

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
    }

    @Test
    void healthEndpointWorks() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void userEndpointsWork() throws Exception {
        String createBody = """
                {
                  "firstName": "Temp",
                  "lastName": "Manager",
                  "email": "temp.manager@test.local",
                  "role": "MANAGER"
                }
                """;

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());

        mockMvc.perform(get("/api/users/{id}", manager.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("efimchik@test.local"));

        String createResponse = mockMvc.perform(
                        post("/api/users")
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
                  "role": "MANAGER"
                }
                """;

        mockMvc.perform(
                        put("/api/users/{id}", createdId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated.manager@test.local"));

        mockMvc.perform(delete("/api/users/{id}", createdId))
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

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].registrationNumber").exists());

        mockMvc.perform(get("/api/vehicles/{id}", vehicle.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("EF-9011"));

        String createResponse = mockMvc.perform(
                        post("/api/vehicles")
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
                        put("/api/vehicles/{id}", createdId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("TEMP-9002"));

        mockMvc.perform(delete("/api/vehicles/{id}", createdId))
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

        mockMvc.perform(get("/api/shipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trackingNumber").value("SHIP-6001"));

        mockMvc.perform(get("/api/shipments").param("optimized", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trackingNumber").value("SHIP-6001"));

        mockMvc.perform(get("/api/shipments/{id}", shipment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingNumber").value("SHIP-6001"));

        String createResponse = mockMvc.perform(
                        post("/api/shipments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingNumber").value("TEMP-SHIP-9001"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(
                        get("/api/shipments/search")
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
                        get("/api/shipments/search")
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
                        get("/api/shipments/search")
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
                        put("/api/shipments/{id}", createdId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destinationCity").value("Budapest"));

        mockMvc.perform(delete("/api/shipments/{id}", createdId))
                .andExpect(status().isNoContent());
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
                        post("/api/shipments/demo/partial-save")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Unexpected internal server error"));

        mockMvc.perform(
                        post("/api/shipments/demo/rollback")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body.replace("DEMO-", "ROLL-"))
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Unexpected internal server error"));
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
}
