package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.DTO.Auth.Response.PatientLabResultResponse;
import com.vivek.HospitalManagement.Entity.LabTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LabTestResultRepository extends JpaRepository<LabTestResult,Long> {

    Optional<LabTestResult> findByOrderId(Long orderId);


    @Query("""
    SELECT new com.vivek.HospitalManagement.DTO.Auth.Response.PatientLabResultResponse(
        r.order.id,
        t.name,
        t.sampleType,
        r.result,
        r.remarks,
        r.order.completedAt
    )
    FROM LabTestResult r
    JOIN r.order o
    JOIN o.labTest t
    JOIN o.patient p
    JOIN p.user u
    WHERE r.order.id = :orderId
      AND u.email = :email
""")
    Optional<PatientLabResultResponse> findPatientResult(
            @Param("orderId") Long orderId,
            @Param("email") String email
    );
}
