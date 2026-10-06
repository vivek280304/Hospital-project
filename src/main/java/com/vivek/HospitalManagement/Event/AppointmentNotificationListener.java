package com.vivek.HospitalManagement.Event;

import com.vivek.HospitalManagement.Service.AppointmentService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AppointmentNotificationListener {

    private final AppointmentService appointmentService;

    public AppointmentNotificationListener(
            AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAppointmentBooked(
            AppointmentBookedEvent event) {

        try {
            appointmentService.sendConfirmationEmail(
                    event.appointmentId()
            );

        } catch (Exception e) {

            System.err.println(
                    "Failed to send appointment confirmation email for appointment: "
                            + event.appointmentId()
            );

            e.printStackTrace();
        }
    }
}