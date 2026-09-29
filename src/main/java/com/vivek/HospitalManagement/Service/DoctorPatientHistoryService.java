package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.DTO.Auth.Response.*;
import com.vivek.HospitalManagement.Entity.*;
import com.vivek.HospitalManagement.Exceptions.ResourceNotFoundException;
import com.vivek.HospitalManagement.Repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorPatientHistoryService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalReportRepository medicalReportRepository;
    private final LabTestOrderRepository labTestOrderRepository;
    private final LabTestResultRepository labTestResultRepository;
    private final DiagnosticImageRepository diagnosticImageRepository;

    public DoctorPatientHistoryService(
            UserRepository userRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            MedicalReportRepository medicalReportRepository,
            LabTestOrderRepository labTestOrderRepository,
            LabTestResultRepository labTestResultRepository,
            DiagnosticImageRepository diagnosticImageRepository
    ) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.medicalReportRepository = medicalReportRepository;
        this.labTestOrderRepository = labTestOrderRepository;
        this.labTestResultRepository = labTestResultRepository;
        this.diagnosticImageRepository = diagnosticImageRepository;
    }

    /**
     * Get all patients who have an appointment
     * with the logged-in doctor.
     */
    public List<DoctorPatientResponse> getMyPatients(
            String doctorEmail
    ) {

        Doctor doctor = doctorRepository
                .findByUserEmail(doctorEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found"
                        )
                );

        return appointmentRepository
                .findByDoctorId(doctor.getId())
                .stream()

                .map(Appointment::getPatient)

                .distinct()

                .map(patient ->
                        new DoctorPatientResponse(
                                patient.getId(),
                                patient.getUser().getName(),
                                patient.getUser().getEmail(),
                                patient.getDateOfBirth(),
                                patient.getGender(),
                                patient.getPhoneNumber()
                        )
                )

                .toList();
    }


    /**
     * Get complete history of a patient
     * for the logged-in doctor.
     */
    public DoctorPatientHistoryResponse getPatientHistory(
            String doctorEmail,
            Long patientId
    ) {

        Doctor doctor = doctorRepository
                .findByUserEmail(doctorEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found"
                        )
                );

        Patient patient = patientRepository
                .findById(patientId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found"
                        )
                );

        /*
         * SECURITY
         *
         * Doctor can only access a patient if
         * that patient has an appointment with
         * this doctor.
         */
        boolean hasAppointment =
                appointmentRepository.existsByDoctorIdAndPatientId(
                        doctor.getId(),
                        patientId
                );

        if (!hasAppointment) {
            throw new AccessDeniedException(
                    "You are not allowed to access this patient's history"
            );
        }


        // ------------------------------------------
        // PATIENT INFORMATION
        // ------------------------------------------

        DoctorPatientResponse patientResponse =
                new DoctorPatientResponse(
                        patient.getId(),
                        patient.getUser().getName(),
                        patient.getUser().getEmail(),
                        patient.getDateOfBirth(),
                        patient.getGender(),
                        patient.getPhoneNumber()
                );


        // ------------------------------------------
        // APPOINTMENTS
        // ------------------------------------------

        List<AppointmentResponse> appointments =
                appointmentRepository
                        .findByDoctorIdAndPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(
                                doctor.getId(),
                                patientId
                        )
                        .stream()

                        .map(appointment ->
                                new AppointmentResponse(
                                        appointment.getId(),
                                        patient.getId(),
                                        patient.getUser().getName(),
                                        appointment.getAppointmentDate(),
                                        appointment.getAppointmentTime(),
                                        appointment.getStatus(),
                                        appointment.getReason()
                                )
                        )

                        .toList();


        // ------------------------------------------
        // MEDICAL REPORTS
        // ------------------------------------------

        List<MedicalReportResponse> reports =
                medicalReportRepository
                        .findByPatientId(patientId)
                        .stream()

                        .map(report ->
                                new MedicalReportResponse(
                                        report.getId(),

                                        report.getAppointment().getId(),

                                        report.getPatient().getId(),
                                        report.getPatient().getUser().getName(),

                                        report.getDoctor().getId(),
                                        report.getDoctor().getUser().getName(),

                                        report.getAppointment().getAppointmentDate(),
                                        report.getAppointment().getAppointmentTime(),

                                        report.getDiagnosis(),
                                        report.getSymptoms(),
                                        report.getTreatment(),
                                        report.getPrescription(),
                                        report.getNotes(),

                                        report.getCreatedAt()
                                )
                        )

                        .toList();


        // ------------------------------------------
        // LAB TESTS
        // ------------------------------------------

        List<LabTestOrder> orders =
                labTestOrderRepository
                        .findByPatientIdOrderByScheduledDateDescScheduledTimeDesc(
                                patientId
                        );

        List<PatientLabOrderResponse> labTests =
                orders
                        .stream()

                        .map(order ->
                                new PatientLabOrderResponse(
                                        order.getId(),
                                        order.getLabTest().getName(),
                                        order.getLabTest().getSampleType(),
                                        order.getScheduledDate(),
                                        order.getScheduledTime(),
                                        order.getStatus()
                                )
                        )

                        .toList();


        // ------------------------------------------
        // LAB RESULTS
        // ------------------------------------------

        List<PatientLabResultResponse> labResults =
                orders
                        .stream()

                        .map(order ->
                                labTestResultRepository
                                        .findByOrderId(order.getId())
                                        .map(result ->
                                                new PatientLabResultResponse(
                                                        order.getId(),
                                                        order.getLabTest().getName(),
                                                        order.getLabTest().getSampleType(),
                                                        result.getResult(),
                                                        result.getRemarks(),
                                                        result.getCreatedAt()
                                                )
                                        )
                                        .orElse(null)
                        )

                        .filter(result -> result != null)

                        .toList();


        // ------------------------------------------
        // IMAGING / X-RAYS
        // ------------------------------------------

        List<DiagnosticImageResponse> imaging =
                diagnosticImageRepository
                        .findByPatientIdAndAppointmentDoctorId(
                                patientId,
                                doctor.getId()
                        )
                        .stream()

                        .map(image ->
                                new DiagnosticImageResponse(
                                        image.getId(),

                                        image.getPatient().getId(),

                                        image.getPatient()
                                                .getUser()
                                                .getName(),

                                        image.getAppointment() != null
                                                ? image.getAppointment().getId()
                                                : null,

                                        image.getUploadedBy().getId(),

                                        image.getUploadedBy().getName(),

                                        image.getImagingType(),

                                        image.getFileName(),

                                        image.getFileSize(),

                                        image.getUploadedAt()
                                )
                        )

                        .toList();


        return new DoctorPatientHistoryResponse(
                patientResponse,
                appointments,
                reports,
                labTests,
                labResults,
                imaging
        );
    }
}