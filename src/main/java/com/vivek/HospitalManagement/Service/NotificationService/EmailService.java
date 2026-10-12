package com.vivek.HospitalManagement.Service.NotificationService;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import com.vivek.HospitalManagement.Exceptions.EmailSendingException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class EmailService {

    private final MailjetClient mailjetClient;
    private final String fromEmail;

    public EmailService(
            @Value("${mailjet.api-key}") String apiKey,
            @Value("${mailjet.api-secret}") String apiSecret,
            @Value("${mailjet.from-email}") String fromEmail
    ) {
        this.mailjetClient = new MailjetClient(
                ClientOptions.builder()
                        .apiKey(apiKey)
                        .apiSecretKey(apiSecret)
                        .build()
        );

        this.fromEmail = fromEmail;
    }

    /**
     * Sends an email using the Mailjet API.
     */
    private void sendEmail(
            String toEmail,
            String subject,
            String text
    ) {
        try {
            JSONObject message = new JSONObject()
                    .put(
                            "From",
                            new JSONObject()
                                    .put("Email", fromEmail)
                                    .put("Name", "Hospital Management")
                    )
                    .put(
                            "To",
                            new JSONArray()
                                    .put(
                                            new JSONObject()
                                                    .put("Email", toEmail)
                                    )
                    )
                    .put("Subject", subject)
                    .put("TextPart", text);

            MailjetRequest request = new MailjetRequest(
                    Emailv31.resource
            ).property(
                    Emailv31.MESSAGES,
                    new JSONArray().put(message)
            );

            MailjetResponse response = mailjetClient.post(request);

            if (response.getStatus() < 200
                    || response.getStatus() >= 300) {

                throw new EmailSendingException(
                        "Mailjet rejected email to " + toEmail
                                + ". HTTP status: "
                                + response.getStatus(),
                        new RuntimeException(
                                response.getData().toString()
                        )
                );
            }

        } catch (EmailSendingException e) {
            throw e;

        } catch (Exception e) {
            throw new EmailSendingException(
                    "Failed to send email to " + toEmail,
                    e
            );
        }
    }

    /**
     * 1. Password reset OTP email.
     */
    public void sendPasswordResetOtp(
            String toEmail,
            String otp
    ) {
        sendEmail(
                toEmail,
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

    /**
     * 2. Registration OTP email.
     */
    public void sendRegistrationOtp(
            String toEmail,
            String otp
    ) {
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

    /**
     * 3. Appointment confirmation email.
     */
    public void sendAppointmentBookedEmail(
            String toEmail,
            String patientName,
            String doctorName,
            LocalDate appointmentDate,
            LocalTime appointmentTime
    ) {
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

    /**
     * 4. Appointment reminder email.
     */
    public void sendAppointmentReminderEmail(
            String toEmail,
            String patientName,
            String doctorName,
            LocalDate appointmentDate,
            LocalTime appointmentTime
    ) {
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

    /**
     * 5. Notification when a patient is shared with another doctor.
     */
    public void sendPatientSharedEmail(
            String toEmail,
            String juniorDoctorName,
            String seniorDoctorName,
            String patientName
    ) {
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

    /**
     * 6. Notification when an imaging report is uploaded.
     */
    public void sendImagingReportUploadedEmail(
            String toEmail,
            String patientName,
            String imagingType,
            String fileName
    ) {
        sendEmail(
                toEmail,
                "Hospital Management - " + imagingType
                        + " Report Available",
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

    /**
     * 7. Notification when a patient account is created.
     */
    public void sendPatientAccountCreatedEmail(
            String toEmail,
            String patientName,
            String initialPassword
    ) {
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

    /**
     * 8. Login OTP email.
     */
    public void sendLoginOtp(
            String toEmail,
            String otp
    ) {
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