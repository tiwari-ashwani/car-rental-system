package com.carrental.entity;


import com.carrental.dto.VehicleSegment;
import com.carrental.dto.VehicleStatus;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "vehicles",
        indexes = {@Index(name = "idx_vehicle_vin", columnList = "vin")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    /** free text: Car, Van, Scooter etc. */
    @Column(nullable = false)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VehicleSegment segment;

    @Column(nullable = false, unique = true, length = 64)
    private String vin;

    /** model year */
    private Integer modelYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VehicleStatus status;

    // One vehicle -> many bookings
    @OneToMany(mappedBy = "vehicle", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Booking> bookings = new ArrayList<>();
}

