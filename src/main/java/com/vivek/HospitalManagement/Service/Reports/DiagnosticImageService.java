package com.vivek.HospitalManagement.Service.Reports;

import com.vivek.HospitalManagement.DTO.Auth.Request.ImagingUploadRequest;
import com.vivek.HospitalManagement.DTO.Auth.Response.DiagnosticImageResponse;
import com.vivek.HospitalManagement.Entity.*;
import com.vivek.HospitalManagement.Enums.ImagingOrderStatus;
import com.vivek.HospitalManagement.Repository.*;
import com.vivek.HospitalManagement.Service.NotificationService.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiagnosticImageService {

    private final DiagnosticImageRepository diagnosticImageRepository;
    private final UserRepository userRepository;
    private final MinioStorageService minioStorageService;
    private final ImagingOrderRepository imagingOrderRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorPatientShareRepository shareRepository;
    private final EmailService emailService;


    public DiagnosticImageService(DiagnosticImageRepository diagnosticImageRepository, UserRepository userRepository, MinioStorageService minioStorageService, ImagingOrderRepository imagingOrderRepository, DoctorRepository doctorRepository, PatientRepository patientRepository, DoctorPatientShareRepository shareRepository, EmailService emailService) {
        this.diagnosticImageRepository = diagnosticImageRepository;
        this.userRepository = userRepository;
        this.minioStorageService = minioStorageService;
        this.imagingOrderRepository = imagingOrderRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.shareRepository = shareRepository;
        this.emailService = emailService;
    }

    @Transactional
    public DiagnosticImage upload(
            ImagingUploadRequest request,
            MultipartFile file,
            String email){

        ImagingOrder order = imagingOrderRepository
                .findById(request.getOrderId())
                .orElseThrow(() ->
                        new RuntimeException("Imaging order not found"));


        if (order.getStatus() !=
                com.vivek.HospitalManagement.Enums.ImagingOrderStatus.PENDING) {

            throw new RuntimeException(
                    "Imaging order is not pending");
        }

        User lab_Tech = userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("User not found"));

        String storageKey = minioStorageService.uploadFile(file);

        DiagnosticImage image = new DiagnosticImage();

        image.setPatient(order.getPatient());
        image.setUploadedBy(lab_Tech);
        image.setAppointment(order.getAppointment());
        image.setImagingType(order.getImagingType());
        image.setFileName(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setFileSize(file.getSize());
        image.setStorageKey(storageKey);
        image.setUploadedAt(LocalDateTime.now());


        diagnosticImageRepository.save(image);
        order.setStatus(ImagingOrderStatus.COMPLETED);
        imagingOrderRepository.save(order);


         emailService.sendImagingReportUploadedEmail(
                 order.getPatient().getUser().getEmail(),
                 order.getPatient().getUser().getName(),
                 order.getImagingType().name(),
                 image.getFileName());

         return image;

    }

    public DiagnosticImage getImageForDoctor(Long imageID , String doctorEmail){

        Doctor doctor = doctorRepository.findByUserEmail(doctorEmail)
                .orElseThrow(()-> new RuntimeException("Doctor not found"));

        DiagnosticImage image = diagnosticImageRepository.findById(imageID)
                .orElseThrow(()-> new RuntimeException("Image not found"));

        Patient patient = image.getPatient();

        boolean isOwnPatient = false;

        if (image.getAppointment()!= null &&
                image.getAppointment().getDoctor() != null){

            isOwnPatient = image.getAppointment()
                    .getDoctor()
                    .getId()
                    .equals(doctor.getId());
        }

        boolean isSharedDoctor =
                shareRepository
                        .existsByPatientIdAndJuniorDoctorIdAndRevokedAtIsNull(
                                patient.getId(),
                                doctor.getId()
                        );


        if (!isOwnPatient && !isSharedDoctor) {
            throw new RuntimeException(
                    "You do not have access to this imaging"
            );
        }

        return image;
    }

    public List<DiagnosticImageResponse> getMyImage(String email){

        User user = userRepository.findByEmail(email)
                                    .orElseThrow(()->new RuntimeException("User not found"));

        Patient patient = patientRepository.findByUserId(user.getId())
                                     .orElseThrow(()->new RuntimeException("Patient not found"));

        return diagnosticImageRepository.findByPatientId(patient.getId())
                .stream()
                .map(image-> new DiagnosticImageResponse(image.getId(),
                        image.getPatient().getId(),
                        image.getPatient().getUser().getName(),
                        image.getAppointment() != null
                                ? image.getAppointment().getId()
                                : null,
                        image.getUploadedBy().getId(),
                        image.getUploadedBy().getName(),
                        image.getImagingType(),
                        image.getFileName(),
                        image.getFileSize(),
                        image.getUploadedAt()))
                .toList();
    }

    public DiagnosticImage getImageForPatient(Long imageID , String patientEmail){

        User user = userRepository.findByEmail(patientEmail)
                .orElseThrow(()->new RuntimeException("User not found"));

        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(()-> new RuntimeException("Patient not found"));

        DiagnosticImage image = diagnosticImageRepository.findById(imageID)
                .orElseThrow(()-> new RuntimeException("Image not found"));


        if (!image.getPatient().getId().equals(patient.getId())){
            throw new RuntimeException( "You do not have access to this imaging");
        }

        return image;
    }

    public List<DiagnosticImageResponse> getPatientImagingForNurse(Long patientID){


       return diagnosticImageRepository.findByPatientId(patientID)
               .stream()
               .map(image-> new DiagnosticImageResponse(image.getId(),
                       image.getPatient().getId(),
                       image.getPatient().getUser().getName(),
                       image.getAppointment()!= null
                        ? image.getAppointment().getId() : null,
                       image.getUploadedBy().getId(),
                       image.getUploadedBy().getName(),
                       image.getImagingType(),
                       image.getFileName(),
                       image.getFileSize(),
                       image.getUploadedAt()))
               .toList();


    }

    public DiagnosticImage getImageForNurse(Long imageID){

        return  diagnosticImageRepository.findById(imageID)
                                    .orElseThrow(()-> new RuntimeException("Image not found"));


    }

    public List<DiagnosticImageResponse> getSharedPatientImaging(
            Long patientId,
            String doctorEmail) {

        Doctor doctor = doctorRepository
                .findByUserEmail(doctorEmail)
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found"));

        boolean hasAccess =
                shareRepository
                        .existsByPatientIdAndJuniorDoctorIdAndRevokedAtIsNull(
                                patientId,
                                doctor.getId()
                        );

        if (!hasAccess) {
            throw new RuntimeException(
                    "You do not have access to this patient's imaging"
            );
        }

        return diagnosticImageRepository
                .findByPatientId(patientId)
                .stream()
                .map(image -> new DiagnosticImageResponse(
                        image.getId(),
                        image.getPatient().getId(),
                        image.getPatient().getUser().getName(),
                        image.getAppointment() != null
                                ? image.getAppointment().getId()
                                : null,
                        image.getUploadedBy().getId(),
                        image.getUploadedBy().getName(),
                        image.getImagingType(),
                        image.getFileName(),
                        image.getFileSize(),
                        image.getUploadedAt()
                ))
                .toList();
    }

}
