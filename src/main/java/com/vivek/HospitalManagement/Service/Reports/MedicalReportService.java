package com.vivek.HospitalManagement.Service.Reports;

import com.vivek.HospitalManagement.DTO.Auth.Response.MedicalReportResponse;
import com.vivek.HospitalManagement.Entity.Doctor;
import com.vivek.HospitalManagement.Entity.MedicalReport;
import com.vivek.HospitalManagement.Entity.Patient;
import com.vivek.HospitalManagement.Entity.User;
import com.vivek.HospitalManagement.Exceptions.ResourceNotFoundException;
import com.vivek.HospitalManagement.Repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicalReportService {

    private final MedicalReportRepository medicalReportRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorPatientShareRepository shareRepository;
    private final AppointmentRepository appointmentRepository;

    public MedicalReportService(MedicalReportRepository medicalReportRepository,
                                UserRepository userRepository,
                                PatientRepository patientRepository,
                                DoctorRepository doctorRepository,
                                DoctorPatientShareRepository shareRepository,
                                AppointmentRepository appointmentRepository) {


        this.medicalReportRepository = medicalReportRepository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.shareRepository = shareRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<MedicalReportResponse> getPatientReportsForNurse(
            Long patientId) {

        return medicalReportRepository
                .findByPatientId(patientId)
                .stream()
                .map(report -> new MedicalReportResponse(
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
                ))
                .toList();
    }

    public List<MedicalReportResponse> getMyReports(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Patient profile not found"));

        return medicalReportRepository.findByPatientId(patient.getId())
                .stream()
                .map(report -> new MedicalReportResponse(
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
                ))
                .toList();
    }

    public  List<MedicalReportResponse> getSharedReport(String doctorEmail,Long patientId){


        Doctor doctor = doctorRepository.findByUserEmail(doctorEmail)
                .orElseThrow(()->new RuntimeException("Doctor not found"));


        boolean hasAccess = shareRepository.
                            existsByPatientIdAndJuniorDoctorIdAndRevokedAtIsNull(
                                    patientId,doctor.getId()
                            );

        if (!hasAccess) {
            throw new RuntimeException(
                    "You do not have access to this patient's reports"
            );
        }

        return medicalReportRepository
                .findByPatientId(patientId)
                .stream()
                .map(report -> new MedicalReportResponse(
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
                ))
                .toList();
    }

    public List<MedicalReportResponse> getPatientReportsForDoctor(
            String email,
            Long patientId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor profile not found"));

        patientRepository.findById(patientId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Patient not found"));

        boolean hasAppointment =
                appointmentRepository.existsByDoctorIdAndPatientId(
                        doctor.getId()
                        ,patientId);

        if (!hasAppointment) {
            throw new AccessDeniedException(
                    "You are not allowed to access this patient's reports"
            );
        }

        List<MedicalReport> reports =
                medicalReportRepository.findByPatientId(patientId);

        return reports.stream()
                .map(report ->
                        new MedicalReportResponse(
                                report.getId(),
                                report.getAppointment().getId(),
                                report.getPatient().getId(),
                                report.getDoctor().getId(),
                                report.getDiagnosis(),
                                report.getSymptoms(),
                                report.getTreatment(),
                                report.getPrescription(),
                                report.getNotes(),
                                report.getCreatedAt()
                        )
                )
                .toList();
    }

}
