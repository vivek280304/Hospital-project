package com.vivek.HospitalManagement.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "appointment_slot_holds",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_doctor_slot_hold",
                        columnNames = {
                                "doctor_id",
                                "appointment_date",
                                "appointment_time"
                        }
                )
        }
)
@Getter
@Setter
public class AppointmentSlotHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}