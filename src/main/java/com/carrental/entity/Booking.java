package com.carrental.entity;

import com.carrental.dto.VehicleSegment;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bookings",
        indexes = {
                @Index(name = "idx_booking_customer", columnList = "customer_id"),
                @Index(name = "idx_booking_vehicle", columnList = "vehicle_id"),
                @Index(name = "idx_booking_start_end", columnList = "start_date, end_date")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String licenseNumber;

    @Column(nullable = true)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleSegment segment;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private long rentalDays;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal rentalPrice;

    @Column(nullable = false)
    private Integer age;

    // Many bookings -> one customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_customer"))
    private Customer customer;

    // Many bookings -> one vehicle
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_vehicle"))
    private Vehicle vehicle;
}

