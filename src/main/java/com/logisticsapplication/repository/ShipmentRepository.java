package com.logisticsapplication.repository;

import com.logisticsapplication.model.Shipment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Integer> {

    List<Shipment> findByStatusIgnoreCase(String status);
}
