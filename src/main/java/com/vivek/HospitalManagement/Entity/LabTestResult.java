package com.vivek.HospitalManagement.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "lab_test_result")
@Getter
@Setter
@NoArgsConstructor
public class LabTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private LabTestOrder order;

    @Column(nullable = false, length = 5000)
    private String result;

    @Column(length = 2000)
    private String remarks;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}