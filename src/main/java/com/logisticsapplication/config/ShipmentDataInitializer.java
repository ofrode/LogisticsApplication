package com.logisticsapplication.config;

import com.logisticsapplication.model.Shipment;
import com.logisticsapplication.repository.ShipmentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShipmentDataInitializer implements CommandLineRunner {

    private final ShipmentRepository shipmentRepository;

    @Override
    public void run(String... args) {
        if (shipmentRepository.count() > 0) {
            return;
        }

        shipmentRepository.save(new Shipment(
                null,
                "Industrial Equipment",
                "Minsk",
                "Warsaw",
                LocalDate.now().plusDays(1),
                new BigDecimal("1800.50"),
                "NEW"
        ));

        shipmentRepository.save(new Shipment(
                null,
                "Textiles",
                "Brest",
                "Vilnius",
                LocalDate.now().plusDays(2),
                new BigDecimal("730.00"),
                "IN_PROGRESS"
        ));

        shipmentRepository.save(new Shipment(
                null,
                "Auto Parts",
                "Gomel",
                "Riga",
                LocalDate.now().plusDays(3),
                new BigDecimal("950.25"),
                "NEW"
        ));
    }
}
