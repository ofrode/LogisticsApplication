package com.logisticsapplication.repository;

import com.logisticsapplication.model.Shipment;
import com.logisticsapplication.model.ShipmentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    List<Shipment> findByStatus(ShipmentStatus status);

    @EntityGraph(attributePaths = {"customer", "manager", "cargoes", "schedule", "vehicles"})
    @Query("select s from Shipment s")
    List<Shipment> findAllWithDetails();

    @EntityGraph(attributePaths = {"customer", "manager", "cargoes", "schedule", "vehicles"})
    List<Shipment> findByStatusOrderByIdAsc(ShipmentStatus status);

    @EntityGraph(attributePaths = {"customer", "manager", "cargoes", "schedule", "vehicles"})
    Optional<Shipment> findDetailedById(Long id);
}
