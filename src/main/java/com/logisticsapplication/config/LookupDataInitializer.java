package com.logisticsapplication.config;

import com.logisticsapplication.model.ShipmentStatus;
import com.logisticsapplication.model.ShipmentStatusLookup;
import com.logisticsapplication.model.UserRole;
import com.logisticsapplication.model.UserRoleLookup;
import com.logisticsapplication.repository.ShipmentStatusLookupRepository;
import com.logisticsapplication.repository.UserRoleLookupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LookupDataInitializer implements ApplicationRunner {

    private final UserRoleLookupRepository userRoleLookupRepository;
    private final ShipmentStatusLookupRepository shipmentStatusLookupRepository;

    @Override
    public void run(ApplicationArguments args) {
        for (UserRole role : UserRole.values()) {
            userRoleLookupRepository.findByCode(role.name()).orElseGet(
                    () -> userRoleLookupRepository.save(new UserRoleLookup(null, role.name()))
            );
        }
        for (ShipmentStatus status : ShipmentStatus.values()) {
            shipmentStatusLookupRepository.findByCode(status.name()).orElseGet(
                    () -> shipmentStatusLookupRepository.save(
                            new ShipmentStatusLookup(null, status.name())
                    )
            );
        }
    }
}
