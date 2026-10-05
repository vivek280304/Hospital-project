package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.AppointmentSlotHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

public interface AppointmentSlotHoldRepository
        extends JpaRepository<AppointmentSlotHold, Long> {

    Optional<AppointmentSlotHold>
    findByDoctorIdAndAppointmentDateAndAppointmentTime(
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime appointmentTime
    );

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTime(
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime appointmentTime
    );

    Optional<AppointmentSlotHold> findByOrderId(String orderId);

    @Modifying
    @Query("""
        DELETE FROM AppointmentSlotHold h
        WHERE h.expiresAt <= :now
    """)
    int deleteExpiredHolds(@Param("now") LocalDateTime now);
}