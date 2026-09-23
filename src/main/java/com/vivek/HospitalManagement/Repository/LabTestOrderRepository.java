package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.DTO.Auth.Response.LabOrderResponse;
import com.vivek.HospitalManagement.DTO.Auth.Response.PatientLabOrderResponse;
import com.vivek.HospitalManagement.Entity.LabTestOrder;
import com.vivek.HospitalManagement.Enums.LabTestOrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabTestOrderRepository extends JpaRepository<LabTestOrder,Long> {

    @Query("""
    SELECT new com.vivek.HospitalManagement.DTO.Auth.Response.LabOrderResponse(
        o.id,
        pu.name,
        t.name,
        t.sampleType,
        o.scheduledDate,
        o.scheduledTime,
        o.status
    )
    FROM LabTestOrder o
    JOIN o.patient p
    JOIN p.user pu
    JOIN o.labTest t
    WHERE o.status = :status
    ORDER BY o.scheduledDate ASC, o.scheduledTime ASC
""")
    List<LabOrderResponse> findOrdersByStatus(
            @Param("status") LabTestOrderStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT o
    FROM LabTestOrder o
    WHERE o.id = :orderId
""")
    Optional<LabTestOrder> findByIdForUpdate(
            @Param("orderId") Long orderId
    );

        @Query("""
        SELECT new com.vivek.HospitalManagement.DTO.Auth.Response.PatientLabOrderResponse(
            o.id,
            t.name,
            t.sampleType,
            o.scheduledDate,
            o.scheduledTime,
            o.status
        )
        FROM LabTestOrder o
        JOIN o.labTest t
        JOIN o.patient p
        JOIN p.user u
        WHERE u.email = :email
        ORDER BY o.scheduledDate DESC, o.scheduledTime DESC
    """)
        List<PatientLabOrderResponse> findPatientOrders(
                @Param("email") String email
        );
    }

