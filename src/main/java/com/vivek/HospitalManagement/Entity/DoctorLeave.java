package com.vivek.HospitalManagement.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "doctor_leaves",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"doctor_id", "leave_date"}
                )
        }
)
public class DoctorLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "leave_date", nullable = false)
    private LocalDate leaveDate;

    private String reason;


}