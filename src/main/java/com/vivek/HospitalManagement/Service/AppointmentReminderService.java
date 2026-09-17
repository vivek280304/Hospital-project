package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.Entity.Appointment;
import com.vivek.HospitalManagement.Enums.AppointmentStatus;
import com.vivek.HospitalManagement.Repository.AppointmentRepository;
import com.vivek.HospitalManagement.Service.NotificationService.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentReminderService {

    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;

    public AppointmentReminderService(
            AppointmentRepository appointmentRepository,
            EmailService emailService) {

        this.appointmentRepository = appointmentRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendAppointmentReminders() {

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Appointment> appointments =
                appointmentRepository
                        .findByAppointmentDateAndStatus(
                                tomorrow,
                                AppointmentStatus.BOOKED
                        );

        for (Appointment appointment : appointments) {

            if (appointment.isReminderSent()) {
                continue;
            }

            emailService.sendAppointmentReminderEmail(
                    appointment.getPatient().getUser().getEmail(),
                    appointment.getPatient().getUser().getName(),
                    appointment.getDoctor().getUser().getName(),
                    appointment.getAppointmentDate(),
                    appointment.getAppointmentTime()
            );

            appointment.setReminderSent(true);
            appointmentRepository.save(appointment);
        }
    }
}