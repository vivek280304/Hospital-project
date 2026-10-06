package com.vivek.HospitalManagement.Service.Payment;

import com.vivek.HospitalManagement.DTO.Payment.*;
import com.vivek.HospitalManagement.Entity.*;
import com.vivek.HospitalManagement.Enums.AppointmentStatus;
import com.vivek.HospitalManagement.Enums.PaymentStatus;
import com.vivek.HospitalManagement.Event.AppointmentBookedEvent;
import com.vivek.HospitalManagement.Exceptions.ResourceNotFoundException;
import com.vivek.HospitalManagement.Repository.AppointmentRepository;
import com.vivek.HospitalManagement.Repository.AppointmentSlotHoldRepository;
import com.vivek.HospitalManagement.Repository.PatientRepository;
import com.vivek.HospitalManagement.Repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {


    private final PatientRepository patientRepository;
    private final AppointmentSlotHoldRepository appointmentSlotHoldRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final CashfreeService cashfreeService;


    public PaymentService(
            PatientRepository patientRepository, AppointmentSlotHoldRepository appointmentSlotHoldRepository,
            ApplicationEventPublisher eventPublisher,
            PaymentRepository paymentRepository,
            AppointmentRepository appointmentRepository,
            CashfreeService cashfreeService) {

        this.patientRepository = patientRepository;
        this.appointmentSlotHoldRepository = appointmentSlotHoldRepository;
        this.eventPublisher = eventPublisher;
        this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository;
        this.cashfreeService = cashfreeService;


    }

    public PaymentResponse createPayment(CreatePaymentRequest request){

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(()-> new ResourceNotFoundException("appointment not found"));

        paymentRepository.findByAppointmentId(request.getAppointmentId())
                .ifPresent(payment -> {
                    throw new RuntimeException(
                            "Payment already exists for this appointment");


                });

        BigDecimal amount = appointment.getDoctor().getConsultationFee();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(
                    "Doctor consultation fee is not configured"
            );
        }

        String orderId = "ORDER_" + UUID.randomUUID();

        String customerId = String.valueOf(appointment.getPatient().getId());

        String customerPhone = appointment.getPatient().getPhoneNumber();

        CashfreeCreateOrderReponse cashfreeOrderResponse = cashfreeService.createOrder(orderId,amount,customerId,customerPhone);


        Payment payment = new Payment();

        payment.setOrderId(orderId);
        payment.setCfOrderId(cashfreeOrderResponse.getCfOrderId());
        payment.setAmount(amount);
        payment.setAppointment(appointment);
        payment.setStatus(PaymentStatus.CREATED);

        paymentRepository.save(payment);

       return new PaymentResponse(payment.getOrderId(),
                                   cashfreeOrderResponse.getPaymentSessionId(),
                                   payment.getAmount());
    }


    @Transactional
    public void processSuccessfulPayment(String orderId) {

        // 1. Find payment FIRST
        Payment payment = paymentRepository
                .findByOrderId(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment not found for order: " + orderId
                        ));

        // 2. Idempotency check
        // If this webhook was already processed, simply return.
        if (payment.getStatus() == PaymentStatus.SUCCESS
                && payment.getAppointment() != null) {

            System.out.println(
                    "Webhook already processed for order: " + orderId
            );

            return;
        }

        // 3. Verify order directly with Cashfree
        CashfreeOrderResponse cashfreeResponse =
                cashfreeService.getOrderStatus(orderId);

        System.out.println("==============================");
        System.out.println("Cashfree Order Verification");
        System.out.println("Order ID: " + orderId);
        System.out.println("Order Status: " + cashfreeResponse.getOrderStatus());
        System.out.println("Order Amount: " + cashfreeResponse.getOrderAmount());
        System.out.println("==============================");

        if (!"PAID".equalsIgnoreCase(cashfreeResponse.getOrderStatus())) {
            throw new RuntimeException(
                    "Cashfree order is not PAID: " + orderId
            );
        }

        if (cashfreeResponse.getOrderAmount() == null
                || payment.getAmount() == null
                || cashfreeResponse.getOrderAmount()
                .compareTo(payment.getAmount()) != 0) {

            throw new RuntimeException(
                    "Payment amount mismatch for order: " + orderId
            );
        }

        // 3. Find temporary slot hold
        AppointmentSlotHold hold = appointmentSlotHoldRepository
                .findByOrderId(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Slot hold not found for order: " + orderId
                        ));

        // 4. Check hold expiry
        if (hold.getExpiresAt().isBefore(LocalDateTime.now())) {

            appointmentSlotHoldRepository.delete(hold);

            throw new RuntimeException(
                    "Slot hold has expired"
            );
        }

        // 5. Check whether slot is already booked
        boolean alreadyBooked =
                appointmentRepository
                        .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
                                hold.getDoctor().getId(),
                                hold.getAppointmentDate(),
                                hold.getAppointmentTime(),
                                AppointmentStatus.BOOKED
                        );

        if (alreadyBooked) {
            throw new RuntimeException(
                    "Appointment slot is already booked"
            );
        }

        // 6. Find patient
        Patient patient = patientRepository
                .findById(hold.getPatientId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Patient not found"
                        ));

        Doctor doctor = hold.getDoctor();

        // 7. Generate booking key
        String bookingKey =
                doctor.getId()
                        + "-"
                        + hold.getAppointmentDate()
                        + "-"
                        + hold.getAppointmentTime();

        // 8. Create appointment
        Appointment appointment = new Appointment();

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(
                hold.getAppointmentDate()
        );
        appointment.setAppointmentTime(
                hold.getAppointmentTime()
        );
        appointment.setStatus(
                AppointmentStatus.BOOKED
        );
        appointment.setBookingKey(bookingKey);

        appointmentRepository.save(appointment);

        // 9. Update payment
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setAppointment(appointment);

        paymentRepository.save(payment);

        // 10. Delete temporary hold
        appointmentSlotHoldRepository.delete(hold);

        eventPublisher.publishEvent(
                new AppointmentBookedEvent(appointment.getId())
        );

        System.out.println(
                "Appointment successfully created: "
                        + appointment.getId()
        );
    }

    @Transactional
    public PaymentStatusResponse getPaymentStatus(String orderId) {

        Payment payment = paymentRepository
                .findByOrderId(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment not found for order: " + orderId
                        ));

        Appointment appointment = payment.getAppointment();

        if (appointment == null) {

            return new PaymentStatusResponse(
                    payment.getStatus().name(),
                    null,
                    payment.getAmount(),
                    null,
                    null
            );
        }

        return new PaymentStatusResponse(
                payment.getStatus().name(),
                appointment.getId(),
                payment.getAmount(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
        );
    }


}