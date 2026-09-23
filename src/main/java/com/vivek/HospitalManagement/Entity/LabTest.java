package com.vivek.HospitalManagement.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


    @Entity
    @Table(name = "lab_test")
    @Getter
    @Setter
    @NoArgsConstructor
    public class LabTest {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, unique = true)
        private String name;

        @Column(length = 500)
        private String description;

        @Column(nullable = false)
        private BigDecimal price;

        @Column(nullable = false)
        private String sampleType;

        @Column(nullable = false)
        private boolean active = true;

}
