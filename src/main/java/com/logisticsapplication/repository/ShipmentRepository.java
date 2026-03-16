package com.logisticsapplication.repository;

import com.logisticsapplication.model.Shipment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            select distinct s.id
            from Shipment s
            join s.customer customer
            join s.cargoes cargo
            join s.schedule schedule
            where (:customerEmail is null or lower(customer.email) = lower(:customerEmail))
              and (:cargoName is null
                  or lower(cargo.name) like lower(concat('%', :cargoName, '%')))
              and (:arrivalFrom is null or schedule.arrivalAt >= :arrivalFrom)
              and (:arrivalTo is null or schedule.arrivalAt <= :arrivalTo)
            """)
    Page<Long> searchIdsJpql(
            @Param("customerEmail") String customerEmail,
            @Param("cargoName") String cargoName,
            @Param("arrivalFrom") LocalDateTime arrivalFrom,
            @Param("arrivalTo") LocalDateTime arrivalTo,
            Pageable pageable
    );

    @Query(
            value = """
                    select distinct s.id
                    from shipments s
                    join app_users customer on customer.id = s.customer_id
                    join cargoes cargo on cargo.shipment_id = s.id
                    join shipment_schedules schedule on schedule.shipment_id = s.id
                    where (:customerEmail is null
                        or lower(customer.email) = lower(cast(:customerEmail as varchar)))
                      and (
                        :cargoName is null
                        or lower(cargo.name) like lower(
                            concat('%', cast(:cargoName as varchar), '%')
                        )
                      )
                      and (:arrivalFrom is null or schedule.arrival_at >= :arrivalFrom)
                      and (:arrivalTo is null or schedule.arrival_at <= :arrivalTo)
                    """,
            countQuery = """
                    select count(distinct s.id)
                    from shipments s
                    join app_users customer on customer.id = s.customer_id
                    join cargoes cargo on cargo.shipment_id = s.id
                    join shipment_schedules schedule on schedule.shipment_id = s.id
                    where (:customerEmail is null
                        or lower(customer.email) = lower(cast(:customerEmail as varchar)))
                      and (
                        :cargoName is null
                        or lower(cargo.name) like lower(
                            concat('%', cast(:cargoName as varchar), '%')
                        )
                      )
                      and (:arrivalFrom is null or schedule.arrival_at >= :arrivalFrom)
                      and (:arrivalTo is null or schedule.arrival_at <= :arrivalTo)
                    """,
            nativeQuery = true
    )
    Page<Long> searchIdsNative(
            @Param("customerEmail") String customerEmail,
            @Param("cargoName") String cargoName,
            @Param("arrivalFrom") LocalDateTime arrivalFrom,
            @Param("arrivalTo") LocalDateTime arrivalTo,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"customer", "manager", "cargoes", "schedule", "vehicles"})
    @Query("select distinct s from Shipment s where s.id in :ids")
    List<Shipment> findAllDetailedByIdIn(@Param("ids") List<Long> ids);
}
