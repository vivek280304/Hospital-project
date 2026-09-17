package com.vivek.HospitalManagement.Entity;

import com.vivek.HospitalManagement.Enums.ImagingOrderStatus;
import com.vivek.HospitalManagement.Enums.ImagingType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class ImagingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private Doctor requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImagingType imagingType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImagingOrderStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;


}
