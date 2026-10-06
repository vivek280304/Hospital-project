package com.vivek.HospitalManagement.Event;

import org.springframework.context.ApplicationEventPublisher;

public record AppointmentBookedEvent(Long appointmentId) {


}
