package com.vivek.HospitalManagement.Service.Reports;

import com.vivek.HospitalManagement.DTO.Auth.Request.ImagingOrderRequest;
import com.vivek.HospitalManagement.DTO.Auth.Response.ImagingOrderResponse;
import com.vivek.HospitalManagement.Entity.Appointment;
import com.vivek.HospitalManagement.Entity.Doctor;
import com.vivek.HospitalManagement.Entity.ImagingOrder;
import com.vivek.HospitalManagement.Entity.Patient;
import com.vivek.HospitalManagement.Enums.ImagingOrderStatus;
import com.vivek.HospitalManagement.Repository.AppointmentRepository;
import com.vivek.HospitalManagement.Repository.DoctorRepository;
import com.vivek.HospitalManagement.Repository.ImagingOrderRepository;
import com.vivek.HospitalManagement.Repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ImagingOrderService {

    private final ImagingOrderRepository imagingOrderRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    public ImagingOrderService(
            ImagingOrderRepository imagingOrderRepository,
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            DoctorRepository doctorRepository) {

        this.imagingOrderRepository = imagingOrderRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
    }

    public ImagingOrder createOrder(
            ImagingOrderRequest request,
            String doctorEmail) {

        Patient patient = patientRepository
                .findById(request.getPatientId())
                .orElseThrow(() ->
                        new RuntimeException("Patient not found"));

        Appointment appointment = appointmentRepository
                .findById(request.getAppointmentId())
                .orElseThrow(() ->
                        new RuntimeException("Appointment not found"));

        if (!appointment.getPatient()
                .getId()
                .equals(patient.getId())) {

            throw new RuntimeException(
                    "Appointment does not belong to patient");
        }

        Doctor doctor = doctorRepository
                .findByUserEmail(doctorEmail)
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found"));

        if (!appointment.getDoctor()
                .getId()
                .equals(doctor.getId())) {

            throw new RuntimeException(
                    "You can only order imaging for your own appointment");
        }

        ImagingOrder order = new ImagingOrder();

        order.setPatient(patient);
        order.setAppointment(appointment);
        order.setRequestedBy(doctor);
        order.setImagingType(request.getImagingType());
        order.setStatus(
                com.vivek.HospitalManagement.Enums.ImagingOrderStatus.PENDING
        );
        order.setCreatedAt(LocalDateTime.now());

        return imagingOrderRepository.save(order);
    }

    public List<ImagingOrderResponse> getPendingOrders(){

        return imagingOrderRepository.findByStatus(ImagingOrderStatus.PENDING)
                .stream()
                .map(order-> new ImagingOrderResponse(order.getId(),
                        order.getPatient().getId(),
                        order.getPatient().getUser().getName(),
                        order.getAppointment().getId(),
                        order.getRequestedBy().getId(),
                        order.getRequestedBy().getUser().getName(),
                        order.getImagingType(),
                        order.getStatus().name(),
                        order.getCreatedAt()
                )).toList();
    }

    public List<ImagingOrderResponse> getPendingOrdersForDoctor(String email){

        return imagingOrderRepository.findByRequestedByUserEmail(email)
                .stream()
                .map(order->new ImagingOrderResponse(order.getId(),
                        order.getPatient().getId(),
                        order.getPatient().getUser().getName(),
                        order.getAppointment().getId(),
                        order.getRequestedBy().getId(),
                        order.getRequestedBy().getUser().getName(),
                        order.getImagingType(),
                        order.getStatus().name(),
                        order.getCreatedAt()))
                .toList();
    }
}