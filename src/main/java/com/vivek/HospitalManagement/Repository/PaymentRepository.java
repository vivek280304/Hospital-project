package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

    Optional<Payment>findByOrderId(String orderId);

    Optional<Payment>findByAppointmentId(Long appointmentId);

    boolean existsByOrderId(String orderId);

}
