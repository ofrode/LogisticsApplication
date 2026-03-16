package com.logisticsapplication.repository;

import com.logisticsapplication.model.UserRoleLookup;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleLookupRepository extends JpaRepository<UserRoleLookup, Long> {

    Optional<UserRoleLookup> findByCode(String code);
}
