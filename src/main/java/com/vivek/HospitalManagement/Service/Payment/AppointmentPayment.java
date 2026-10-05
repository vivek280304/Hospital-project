package com.vivek.HospitalManagement.Service.Payment;

import com.vivek.HospitalManagement.DTO.Auth.Request.BookAppointmentRequest;
import com.vivek.HospitalManagement.DTO.Payment.CashfreeCreateOrderReponse;
import com.vivek.HospitalManagement.DTO.Payment.SlotPaymentResponse;
import com.vivek.HospitalManagement.Entity.*;
import com.vivek.HospitalManagement.Enums.PaymentStatus;
import com.vivek.HospitalManagement.Exceptions.BadRequestException;
import com.vivek.HospitalManagement.Exceptions.ResourceNotFoundException;
import com.vivek.HospitalManagement.Repository.*;
import com.vivek.HospitalManagement.Service.SlotHoldService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.UUID;

@Service
public class AppointmentPayment {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final CashfreeService cashfreeService;
    private final PaymentRepository paymentRepository;
    private final SlotHoldService slotHoldService;
    private final DoctorLeaveRepository doctorLeaveRepository;

    public AppointmentPayment(UserRepository userRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository,
                              DoctorScheduleRepository doctorScheduleRepository,
                             CashfreeService cashfreeService,
                              PaymentRepository paymentRepository,
                              SlotHoldService slotHoldService,
                              DoctorLeaveRepository doctorLeaveRepository) {


        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.cashfreeService = cashfreeService;
        this.paymentRepository = paymentRepository;
        this.slotHoldService = slotHoldService;
        this.doctorLeaveRepository = doctorLeaveRepository;
    }


    @Transactional
    public SlotPaymentResponse createAppointmentPayment(String email, BookAppointmentRequest request){

        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));


        // 2. Find doctor
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor not found"));


        // 3. Check doctor leave
        boolean doctorOnLeave =
                doctorLeaveRepository.existsByDoctorIdAndLeaveDate(
                        doctor.getId(),
                        request.getAppointmentDate()
                );

        if (doctorOnLeave) {
            throw new BadRequestException(
                    "Doctor is on leave on " +
                            request.getAppointmentDate()
            );
        }


        DayOfWeek day =
                request.getAppointmentDate().getDayOfWeek();

        doctorScheduleRepository
                .findByDoctorId(doctor.getId())
                .stream()
                .filter(s -> s.getDayOfWeek().equals(day))
                .filter(s ->
                        !request.getAppointmentTime()
                                .isBefore(s.getStartTime())
                                &&
                                !request.getAppointmentTime()
                                        .plusMinutes(s.getSlotDuration())
                                        .isAfter(s.getEndTime())
                )
                .findFirst()
                .orElseThrow(() ->
                        new BadRequestException(
                                "Doctor is not available at this time"
                        ));

        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseGet(()-> {

                    Patient newpatient = new Patient();

                    newpatient.setUser(user);
                    newpatient.setGender(request.getGender());
                    newpatient.setDateOfBirth(request.getDateOfBirth());
                    newpatient.setPhoneNumber(request.getPhoneNumber());


                    return patientRepository.save(newpatient);

                });

        // 6. Create Cashfree order ID

        String orderId =
                "ORDER_" + UUID.randomUUID();

        AppointmentSlotHold hold = slotHoldService.createHold(orderId,
                doctor,
                patient.getId(),
                request.getAppointmentDate(),
                request.getAppointmentTime());

        BigDecimal amount = doctor.getConsultationFee();

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new BadRequestException(
                    "Doctor consultation fee is not configured"
            );
        }

        CashfreeCreateOrderReponse response = cashfreeService.createOrder(orderId,
                amount,
                String.valueOf(patient.getId()),
                patient.getPhoneNumber());


        Payment payment = new Payment();

        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.CREATED);

        paymentRepository.save(payment);


        return  new SlotPaymentResponse(null,
                orderId,
                response.getPaymentSessionId(),
                amount);
    }
}
