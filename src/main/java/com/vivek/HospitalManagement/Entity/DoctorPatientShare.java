package com.vivek.HospitalManagement.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "doctor_patient_share",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_patient_junior_doctor",
                        columnNames = {"patient_id", "junior_doctor_id"}
                )
        }
)
@Getter
@Setter
public class DoctorPatientShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senior_doctor_id", nullable = false)
    private Doctor seniorDoctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "junior_doctor_id", nullable = false)
    private Doctor juniorDoctor;

    @Column(nullable = false)
    private LocalDateTime sharedAt;

    private LocalDateTime revokedAt;
}
