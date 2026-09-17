package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.DTO.Auth.Request.SharePatientRequest;
import com.vivek.HospitalManagement.DTO.Auth.Response.SharedPatientResponse;
import com.vivek.HospitalManagement.Entity.Doctor;
import com.vivek.HospitalManagement.Entity.DoctorPatientShare;
import com.vivek.HospitalManagement.Entity.Patient;

import com.vivek.HospitalManagement.Exceptions.ResourceNotFoundException;
import com.vivek.HospitalManagement.Repository.DoctorPatientShareRepository;
import com.vivek.HospitalManagement.Repository.DoctorRepository;
import com.vivek.HospitalManagement.Repository.PatientRepository;
import com.vivek.HospitalManagement.Service.NotificationService.EmailService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DoctorPatientShareService {

    private final DoctorPatientShareRepository shareRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final EmailService emailService;

    public DoctorPatientShareService(DoctorPatientShareRepository shareRepository,
                                     DoctorRepository doctorRepository,
                                     PatientRepository patientRepository,
                                     EmailService emailService) {

        this.shareRepository = shareRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void share(Long patientID, @Valid @MonotonicNonNull SharePatientRequest request , String doctorEmail){

        Patient patient = patientRepository.findById(patientID)
                .orElseThrow(()-> new ResourceNotFoundException("Patient not found"));

        Doctor seniordoctor = doctorRepository.findByUserEmail(doctorEmail)
                .orElseThrow(()-> new ResourceNotFoundException("Doctor not found"));

        Doctor juniordoctor = doctorRepository.findById(request.getJuniorDoctorID())
                .orElseThrow(()->new ResourceNotFoundException("Patient not found"));


        if (seniordoctor.getId().equals(juniordoctor.getId())){
            throw new RuntimeException("Cannot share to with themselves");
        }

        boolean alreadyShare = shareRepository.findByPatientIdAndJuniorDoctorIdAndRevokedAtIsNull(
                patientID, juniordoctor.getId())
                .isPresent();

        if (alreadyShare){
            throw new RuntimeException("Report already Shared");
        }

        DoctorPatientShare doctorPatientShare = new DoctorPatientShare();

        doctorPatientShare.setPatient(patient);
        doctorPatientShare.setSeniorDoctor(seniordoctor);
        doctorPatientShare.setJuniorDoctor(juniordoctor);
        doctorPatientShare.setSharedAt(LocalDateTime.now());
        doctorPatientShare.setRevokedAt(null);

        shareRepository.save(doctorPatientShare);

        emailService.sendPatientSharedEmail(
                juniordoctor.getUser().getEmail(),
                juniordoctor.getUser().getName(),
                seniordoctor.getUser().getName(),
                patient.getUser().getName()
        );
    }

    public List<SharedPatientResponse> getSharedPatients(String doctorEmail) {

        Doctor doctor = doctorRepository
                .findByUserEmail(doctorEmail)
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found"));

        return shareRepository
                .findByJuniorDoctorIdAndRevokedAtIsNull(doctor.getId())
                .stream()
                .map(share->new SharedPatientResponse(share.getId(),
                        share.getPatient().getId(),
                        share.getPatient().getUser().getName(),
                        share.getSeniorDoctor().getId(),
                        share.getSeniorDoctor().getUser().getName(),
                        share.getSharedAt()))
                .toList();
    }

}
