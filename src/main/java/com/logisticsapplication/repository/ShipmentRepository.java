package com.logisticsapplication.repository;

import com.logisticsapplication.model.Shipment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    @Query("select s from Shipment s where s.status.code = :statusCode")
    List<Shipment> findByStatusCode(String statusCode);

    @EntityGraph(attributePaths = {"customer", "manager", "cargoes", "schedule", "vehicles"})
    @Query("select s from Shipment s")
    List<Shipment> findAllWithDetails();

    @EntityGraph(attributePaths = {"customer", "manager", "cargoes", "schedule", "vehicles"})
    @Query("""
            select s
            from Shipment s
            where s.status.code = :statusCode
            order by s.id asc
            """)
    List<Shipment> findByStatusCodeOrderByIdAsc(String statusCode);

    @EntityGraph(attributePaths = {"customer", "manager", "cargoes", "schedule", "vehicles"})
    Optional<Shipment> findDetailedById(Long id);
}
