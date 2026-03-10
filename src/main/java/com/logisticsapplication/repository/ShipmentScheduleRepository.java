package com.logisticsapplication.repository;

import com.logisticsapplication.model.ShipmentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentScheduleRepository extends JpaRepository<ShipmentSchedule, Long> {
}
