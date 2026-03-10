package com.logisticsapplication;

import com.logisticsapplication.dto.request.CargoRequest;
import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.request.ShipmentScheduleRequest;
import com.logisticsapplication.model.AppUser;
import com.logisticsapplication.model.UserRole;
import com.logisticsapplication.model.Vehicle;
import com.logisticsapplication.repository.AppUserRepository;
import com.logisticsapplication.repository.CargoRepository;
import com.logisticsapplication.repository.ShipmentRepository;
import com.logisticsapplication.repository.ShipmentScheduleRepository;
import com.logisticsapplication.repository.VehicleRepository;
import com.logisticsapplication.service.ShipmentService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.logisticsapplication.model.ShipmentStatus.CREATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class ShipmentTransactionIntegrationTest {

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private ShipmentScheduleRepository shipmentScheduleRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    private AppUser customer;
    private AppUser manager;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        cargoRepository.deleteAll();
        shipmentScheduleRepository.deleteAll();
        shipmentRepository.deleteAll();
        vehicleRepository.deleteAll();
        appUserRepository.deleteAll();

        customer = appUserRepository.save(new AppUser(
                null,
                "Test Customer",
                "customer@test.local",
                UserRole.CUSTOMER,
                null,
                null,
                null
        ));
        manager = appUserRepository.save(new AppUser(
                null,
                "Test Manager",
                "manager@test.local",
                UserRole.MANAGER,
                null,
                null,
                null
        ));
        AppUser carrier = appUserRepository.save(new AppUser(
                null,
                "Test Carrier",
                "carrier@test.local",
                UserRole.CARRIER,
                null,
                null,
                null
        ));
        vehicle = vehicleRepository.save(new Vehicle(
                null,
                "TEST-100",
                new BigDecimal("5000.00"),
                carrier,
                null
        ));
    }

    @Test
    void partialSaveDemoLeavesPersistedRows() {
        ShipmentRequest request = buildRequest("PARTIAL-001");

        assertThrows(IllegalStateException.class, () -> shipmentService.createWithPartialSaveDemo(request));

        assertThat(shipmentRepository.count()).isEqualTo(1);
        assertThat(shipmentScheduleRepository.count()).isEqualTo(1);
        assertThat(cargoRepository.count()).isEqualTo(1);
    }

    @Test
    void rollbackDemoRevertsAllRows() {
        ShipmentRequest request = buildRequest("ROLLBACK-001");

        assertThrows(IllegalStateException.class, () -> shipmentService.createWithRollbackDemo(request));

        assertThat(shipmentRepository.count()).isZero();
        assertThat(shipmentScheduleRepository.count()).isZero();
        assertThat(cargoRepository.count()).isZero();
    }

    private ShipmentRequest buildRequest(String trackingNumber) {
        return new ShipmentRequest(
                trackingNumber,
                "Minsk",
                "Berlin",
                CREATED,
                customer.getId(),
                manager.getId(),
                List.of(vehicle.getId()),
                List.of(
                        new CargoRequest("Paper", new BigDecimal("150.00")),
                        new CargoRequest("Boxes", new BigDecimal("200.00"))
                ),
                new ShipmentScheduleRequest(
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(2),
                        LocalDateTime.now().plusDays(1)
                )
        );
    }
}
