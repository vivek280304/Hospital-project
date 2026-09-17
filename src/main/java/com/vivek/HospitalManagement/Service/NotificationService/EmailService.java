package com.vivek.HospitalManagement.Service.NotificationService;

import com.vivek.HospitalManagement.Exceptions.EmailSendingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    private void sendEmail(
            String toEmail,
            String subject,
            String text) {

        try {
            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);

        } catch (MailException e) {
            throw new EmailSendingException("Failed to send email" , e);
        }
    }

    public void sendPasswordResetOtp(String toEmail, String otp) {


        sendEmail(toEmail,
                "Hospital Management - Password Reset OTP",
                """
                Your password reset OTP is:

                %s

                This OTP will expire in 5 minutes.

                If you did not request a password reset,
                please ignore this email.
                """.formatted(otp)
        );

    }

    public void sendRegistrationOtp(String toEmail, String otp) {

        sendEmail(
                toEmail,
                "Hospital Management - Registration OTP",
                """
                Your registration OTP is:

                %s

                This OTP will expire in 5 minutes.

                If you did not create an account,
                please ignore this email.
                """.formatted(otp)
        );
    }

    public void sendAppointmentBookedEmail(
            String toEmail,
            String patientName,
            String doctorName,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            String reason) {

        sendEmail(
                toEmail,
                "Hospital Management - Appointment Confirmed",
                """
                Dear %s,
    
                Your appointment has been successfully booked.
    
                Appointment Details:
                ---------------------
                Doctor: %s
                Date: %s
                Time: %s
                Reason: %s
    
                Please arrive a few minutes before your appointment time.
    
                Regards,
                Hospital Management System
                """.formatted(
                        patientName,
                        doctorName,
                        appointmentDate,
                        appointmentTime,
                        reason != null ? reason : "Not specified"
                )
        );
    }

    public void sendAppointmentReminderEmail(
            String toEmail,
            String patientName,
            String doctorName,
            LocalDate appointmentDate,
            LocalTime appointmentTime) {

        sendEmail(
                toEmail,
                "Hospital Management - Appointment Reminder",
                """
                Dear %s,
    
                This is a reminder for your upcoming appointment.
    
                Appointment Details:
                ---------------------
                Doctor: %s
                Date: %s
                Time: %s
    
                Please arrive a few minutes before your appointment time.
    
                Regards,
                Hospital Management System
                """.formatted(
                        patientName,
                        doctorName,
                        appointmentDate,
                        appointmentTime
                )
        );
    }

    public void sendPatientSharedEmail(
            String toEmail,
            String juniorDoctorName,
            String seniorDoctorName,
            String patientName) {

        sendEmail(
                toEmail,
                "Hospital Management - Patient Shared With You",
                """
                Dear Dr. %s,
    
                Dr. %s has shared a patient with you.
    
                Patient Details:
                ----------------
                Patient: %s
    
                You can now access the patient's authorized medical
                reports and imaging through the hospital portal.
    
                Please maintain patient confidentiality and access
                medical information only for authorized care.
    
                Regards,
                Hospital Management System
                """.formatted(
                        juniorDoctorName,
                        seniorDoctorName,
                        patientName
                )
        );
    }

    public void sendImagingReportUploadedEmail(
            String toEmail,
            String patientName,
            String imagingType,
            String fileName) {

        sendEmail(
                toEmail,
                "Hospital Management - " + imagingType + " Report Available",
                """
                Dear %s,
    
                Your %s report has been uploaded to the
                Hospital Management System.
    
                Report Details:
                ----------------
                Type: %s
                File: %s
    
                You can log in to the hospital portal to view
                your %s report.
    
                Regards,
                Hospital Management System
                """.formatted(
                        patientName,
                        imagingType,
                        imagingType,
                        fileName,
                        imagingType
                )
        );
    }

    public void sendPatientAccountCreatedEmail(
            String toEmail,
            String patientName,
            String initialPassword) {

        sendEmail(
                toEmail,
                "Hospital Management - Patient Account Created",
                """
                Dear %s,
    
                Your patient account has been created successfully.
    
                Login Details:
                ---------------------
                Email: %s
                Initial Password: %s
    
                You can use these credentials to log in to the
                Hospital Management System.
    
                You may change your password anytime from your account.
    
                Forgot your password?
                Use the "Forgot Password" option on the login page
                to receive a password-reset OTP on this email address.
    
                Regards,
                Hospital Management System
                """.formatted(
                        patientName,
                        toEmail,
                        initialPassword
                )
        );
    }

    public void sendLoginOtp(
            String toEmail,
            String otp) {

        sendEmail(
                toEmail,
                "Hospital Management - Login OTP",
                """
                Your login OTP is:
    
                %s
    
                This OTP will expire in 5 minutes.
    
                If you did not request a login OTP,
                please ignore this email.
    
                Regards,
                Hospital Management System
                """.formatted(otp)
        );
    }

}