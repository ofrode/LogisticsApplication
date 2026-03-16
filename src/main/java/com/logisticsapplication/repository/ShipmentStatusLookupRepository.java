package com.logisticsapplication.repository;

import com.logisticsapplication.model.ShipmentStatusLookup;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentStatusLookupRepository extends JpaRepository<ShipmentStatusLookup, Long> {

    Optional<ShipmentStatusLookup> findByCode(String code);
}
