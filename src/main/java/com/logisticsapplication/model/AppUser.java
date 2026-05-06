package com.logisticsapplication.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private UserRoleLookup role;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Shipment> customerShipments = new ArrayList<>();

    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    private List<Shipment> managedShipments = new ArrayList<>();

    @OneToMany(mappedBy = "assignedCarrier", fetch = FetchType.LAZY)
    private List<Vehicle> vehicles = new ArrayList<>();

    public AppUser(
            Long id,
            String firstName,
            String lastName,
            String email,
            UserRoleLookup role,
            List<Shipment> customerShipments,
            List<Shipment> managedShipments,
            List<Vehicle> vehicles
    ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.login = email;
        this.passwordHash = "";
        this.role = role;
        this.customerShipments = customerShipments;
        this.managedShipments = managedShipments;
        this.vehicles = vehicles;
    }
}
