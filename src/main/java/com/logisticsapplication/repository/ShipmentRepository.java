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

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    @Query("select s from Shipment s where s.status.code = :statusCode")
    List<Shipment> findByStatusCode(String statusCode);

    @EntityGraph(attributePaths = {
            "status",
            "customer",
            "customer.role",
            "manager",
            "manager.role",
            "cargoes",
            "schedule",
            "vehicles",
            "vehicles.assignedCarrier",
            "vehicles.assignedCarrier.role"
    })
    @Query("select s from Shipment s")
    List<Shipment> findAllWithDetails();

    @EntityGraph(attributePaths = {
            "status",
            "customer",
            "customer.role",
            "manager",
            "manager.role",
            "cargoes",
            "schedule",
            "vehicles",
            "vehicles.assignedCarrier",
            "vehicles.assignedCarrier.role"
    })
    @Query("""
            select s
            from Shipment s
            where s.status.code = :statusCode
            order by s.id asc
            """)
    List<Shipment> findByStatusCodeOrderByIdAsc(String statusCode);

    @EntityGraph(attributePaths = {
            "status",
            "customer",
            "customer.role",
            "manager",
            "manager.role",
            "cargoes",
            "schedule",
            "vehicles",
            "vehicles.assignedCarrier",
            "vehicles.assignedCarrier.role"
    })
    Optional<Shipment> findDetailedById(Long id);

    @Query("""
            select distinct s.id
            from Shipment s
            join s.customer customer
            join s.cargoes cargo
            join s.schedule schedule
            where lower(customer.email) = lower(coalesce(:customerEmail, customer.email))
              and lower(cargo.name) like lower(concat('%', coalesce(:cargoName, cargo.name), '%'))
              and schedule.arrivalAt >= coalesce(:arrivalFrom, schedule.arrivalAt)
              and schedule.arrivalAt <= coalesce(:arrivalTo, schedule.arrivalAt)
            """)
    Page<Long> searchIdsJpql(
            @Param("customerEmail") String customerEmail,
            @Param("cargoName") String cargoName,
            @Param("arrivalFrom") LocalDateTime arrivalFrom,
            @Param("arrivalTo") LocalDateTime arrivalTo,
            Pageable pageable
    );

    @Query("""
            select distinct s.id
            from Shipment s
            join s.customer customer
            join s.cargoes cargo
            join s.schedule schedule
            where lower(customer.email) = lower(coalesce(:customerEmail, customer.email))
              and lower(cargo.name) like lower(concat('%', coalesce(:cargoName, cargo.name), '%'))
              and schedule.arrivalAt >= coalesce(:arrivalFrom, schedule.arrivalAt)
              and schedule.arrivalAt <= coalesce(:arrivalTo, schedule.arrivalAt)
            order by s.id asc
            """)
    List<Long> searchIdsJpql(
            @Param("customerEmail") String customerEmail,
            @Param("cargoName") String cargoName,
            @Param("arrivalFrom") LocalDateTime arrivalFrom,
            @Param("arrivalTo") LocalDateTime arrivalTo
    );

    @Query(
            value = """
                    select distinct s.id
                    from shipments s
                    join app_users customer on customer.id = s.customer_id
                    join cargoes cargo on cargo.shipment_id = s.id
                    join shipment_schedules schedule on schedule.shipment_id = s.id
                    where lower(customer.email) = lower(
                        coalesce(cast(:customerEmail as varchar), customer.email)
                    )
                      and lower(cargo.name) like lower(
                        concat('%', coalesce(cast(:cargoName as varchar), cargo.name), '%')
                      )
                      and schedule.arrival_at >= coalesce(
                        cast(:arrivalFrom as timestamp),
                        schedule.arrival_at
                      )
                      and schedule.arrival_at <= coalesce(
                        cast(:arrivalTo as timestamp),
                        schedule.arrival_at
                      )
                    """,
            countQuery = """
                    select count(distinct s.id)
                    from shipments s
                    join app_users customer on customer.id = s.customer_id
                    join cargoes cargo on cargo.shipment_id = s.id
                    join shipment_schedules schedule on schedule.shipment_id = s.id
                    where lower(customer.email) = lower(
                        coalesce(cast(:customerEmail as varchar), customer.email)
                    )
                      and lower(cargo.name) like lower(
                        concat('%', coalesce(cast(:cargoName as varchar), cargo.name), '%')
                      )
                      and schedule.arrival_at >= coalesce(
                        cast(:arrivalFrom as timestamp),
                        schedule.arrival_at
                      )
                      and schedule.arrival_at <= coalesce(
                        cast(:arrivalTo as timestamp),
                        schedule.arrival_at
                      )
                    order by s.id asc
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

    @Query(
            value = """
                    select distinct s.id
                    from shipments s
                    join app_users customer on customer.id = s.customer_id
                    join cargoes cargo on cargo.shipment_id = s.id
                    join shipment_schedules schedule on schedule.shipment_id = s.id
                    where lower(customer.email) = lower(
                        coalesce(cast(:customerEmail as varchar), customer.email)
                    )
                      and lower(cargo.name) like lower(
                        concat('%', coalesce(cast(:cargoName as varchar), cargo.name), '%')
                      )
                      and schedule.arrival_at >= coalesce(
                        cast(:arrivalFrom as timestamp),
                        schedule.arrival_at
                      )
                      and schedule.arrival_at <= coalesce(
                        cast(:arrivalTo as timestamp),
                        schedule.arrival_at
                      )
                    """,
            nativeQuery = true
    )
    List<Long> searchIdsNative(
            @Param("customerEmail") String customerEmail,
            @Param("cargoName") String cargoName,
            @Param("arrivalFrom") LocalDateTime arrivalFrom,
            @Param("arrivalTo") LocalDateTime arrivalTo
    );

    @EntityGraph(attributePaths = {
            "status",
            "customer",
            "customer.role",
            "manager",
            "manager.role",
            "cargoes",
            "schedule",
            "vehicles",
            "vehicles.assignedCarrier",
            "vehicles.assignedCarrier.role"
    })
    @Query("select distinct s from Shipment s where s.id in :ids")
    List<Shipment> findAllDetailedByIdIn(@Param("ids") List<Long> ids);
}
