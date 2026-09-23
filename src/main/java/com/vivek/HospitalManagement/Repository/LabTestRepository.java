package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.DTO.Auth.Response.LabTestResponse;
import com.vivek.HospitalManagement.Entity.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LabTestRepository extends JpaRepository<LabTest,Long> {

    @Query("""
         SELECT new com.vivek.HospitalManagement.DTO.Auth.Response.LabTestResponse(
            l.id,
            l.name,
            l.description,
            l.price,
            l.sampleType
        )
        FROM LabTest l
        WHERE l.active = true
        ORDER BY l.name
    """)
    List<LabTestResponse> findActiveTests();
}
