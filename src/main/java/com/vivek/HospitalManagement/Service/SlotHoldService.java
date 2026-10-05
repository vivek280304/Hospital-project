package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.Entity.AppointmentSlotHold;
import com.vivek.HospitalManagement.Entity.Doctor;
import com.vivek.HospitalManagement.Enums.AppointmentStatus;
import com.vivek.HospitalManagement.Exceptions.AlreadyExistException;
import com.vivek.HospitalManagement.Repository.AppointmentRepository;
import com.vivek.HospitalManagement.Repository.AppointmentSlotHoldRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class SlotHoldService {

    private final AppointmentSlotHoldRepository appointmentSlotHoldRepository;
    private final AppointmentRepository appointmentRepository;

    public SlotHoldService(AppointmentSlotHoldRepository appointmentSlotHoldRepository, AppointmentRepository appointmentRepository) {
        this.appointmentSlotHoldRepository = appointmentSlotHoldRepository;
        this.appointmentRepository = appointmentRepository;
    }

@Transactional
    public AppointmentSlotHold createHold(String orderId,
                                          Doctor doctor,
                                          Long patientId,
                                          LocalDate appointmentDate,
                                          LocalTime appointmentTime){


        boolean alreadyBooked = appointmentRepository.
                existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(doctor.getId(),
                        appointmentDate,appointmentTime,
                        AppointmentStatus.BOOKED);

        if (alreadyBooked) {

            throw new AlreadyExistException("Appointment already booked");

        }


                appointmentSlotHoldRepository.
                findByDoctorIdAndAppointmentDateAndAppointmentTime(doctor.getId(),
                                                                    appointmentDate,
                                                                    appointmentTime)
                .ifPresent((existingHold -> {
                    if (existingHold.getExpiresAt().isBefore(LocalDateTime.now())) {

                        appointmentSlotHoldRepository.delete(existingHold);

                    } else {

                        throw new RuntimeException(
                                "This appointment slot is currently being booked by another patient"
                        );
                    }
                }));

        AppointmentSlotHold appointmentSlotHold = new AppointmentSlotHold();

        appointmentSlotHold.setDoctor(doctor);
        appointmentSlotHold.setAppointmentDate(appointmentDate);
        appointmentSlotHold.setAppointmentTime(appointmentTime);
        appointmentSlotHold.setOrderId(orderId);
        appointmentSlotHold.setPatientId(patientId);
        appointmentSlotHold.setExpiresAt(LocalDateTime.now().plusMinutes(10));


        return appointmentSlotHoldRepository.save(appointmentSlotHold);
    }
}
