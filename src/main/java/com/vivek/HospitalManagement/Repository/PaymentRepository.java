package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;


import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment>findByOrderId(String orderId);

    Optional<Payment>findByAppointmentId(Long appointmentId);

    boolean existsByOrderId(String orderId);

}
