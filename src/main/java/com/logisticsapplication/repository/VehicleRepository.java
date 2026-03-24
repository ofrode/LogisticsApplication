package com.logisticsapplication.repository;

import com.logisticsapplication.model.Vehicle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByRegistrationNumberIgnoreCase(String registrationNumber);

    @EntityGraph(attributePaths = {"assignedCarrier", "assignedCarrier.role"})
    Optional<Vehicle> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"assignedCarrier", "assignedCarrier.role"})
    List<Vehicle> findAllWithAssignedCarrierBy();
}
