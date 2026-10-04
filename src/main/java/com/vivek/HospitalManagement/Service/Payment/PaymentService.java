package com.vivek.HospitalManagement.Service.Payment;

import com.vivek.HospitalManagement.DTO.Payment.CashfreeCreateOrderReponse;
import com.vivek.HospitalManagement.DTO.Payment.CashfreeOrderResponse;
import com.vivek.HospitalManagement.DTO.Payment.CreatePaymentRequest;
import com.vivek.HospitalManagement.DTO.Payment.PaymentResponse;
import com.vivek.HospitalManagement.Entity.Appointment;
import com.vivek.HospitalManagement.Entity.Payment;
import com.vivek.HospitalManagement.Enums.PaymentStatus;
import com.vivek.HospitalManagement.Exceptions.ResourceNotFoundException;
import com.vivek.HospitalManagement.Repository.AppointmentRepository;
import com.vivek.HospitalManagement.Repository.PaymentRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final CashfreeService cashfreeService;

    public PaymentService(PaymentRepository paymentRepository, AppointmentRepository appointmentRepository, CashfreeService cashfreeService) {
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
}