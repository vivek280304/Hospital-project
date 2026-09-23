package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.DTO.Auth.Request.BookLabTestRequest;
import com.vivek.HospitalManagement.DTO.Auth.Request.CompleteLabTestRequest;
import com.vivek.HospitalManagement.DTO.Auth.Response.LabOrderResponse;
import com.vivek.HospitalManagement.DTO.Auth.Response.PatientLabOrderResponse;
import com.vivek.HospitalManagement.DTO.Auth.Response.PatientLabResultResponse;
import com.vivek.HospitalManagement.Entity.*;
import com.vivek.HospitalManagement.Enums.LabTestOrderStatus;
import com.vivek.HospitalManagement.Enums.Role;
import com.vivek.HospitalManagement.Exceptions.BadRequestException;
import com.vivek.HospitalManagement.Exceptions.ResourceNotFoundException;
import com.vivek.HospitalManagement.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class LabTestOrderService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final LabTestRepository labTestRepository;
    private final LabTestOrderRepository labTestOrderRepository;
    private final LabTestResultRepository labTestResultRepository;

    public LabTestOrderService(UserRepository userRepository, PatientRepository patientRepository, LabTestRepository labTestRepository, LabTestOrderRepository labTestOrderRepository, LabTestResultRepository labTestResultRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.labTestRepository = labTestRepository;
        this.labTestOrderRepository = labTestOrderRepository;
        this.labTestResultRepository = labTestResultRepository;
    }

    @Transactional
    public void bookLabTest(
            String email,
            BookLabTestRequest request) {

        // 1. Find logged-in user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // 2. Find patient's profile
        Patient patient = patientRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient profile not found"));

        // 3. Find selected lab test
        LabTest labTest = labTestRepository
                .findById(request.getLabTestId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Test not found"));

        // 4. Make sure test is still active
        if (!labTest.isActive()) {
            throw new BadRequestException(
                    "This lab test is currently unavailable");
        }

        // 5. Create order
        LabTestOrder order = new LabTestOrder();

        order.setPatient(patient);
        order.setLabTest(labTest);
        order.setScheduledDate(request.getScheduledDate());
        order.setScheduledTime(request.getScheduledTime());
        order.setStatus(LabTestOrderStatus.ORDERED);
        order.setOrderedAt(LocalDateTime.now());

        // 6. Save
        labTestOrderRepository.save(order);
    }

    public List<LabOrderResponse> getOrdersByStatus(
            LabTestOrderStatus status) {

        return labTestOrderRepository.findOrdersByStatus(status);
    }

    @Transactional
    public void claimOrder(
            Long orderId,
            String technicianEmail) {

        LabTestOrder order = labTestOrderRepository
                .findByIdForUpdate(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Lab test order not found"));

        if (order.getStatus() != LabTestOrderStatus.ORDERED) {
            throw new BadRequestException(
                    "This order is no longer available to claim");
        }

        User technician = userRepository
                .findByEmail(technicianEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Technician not found"));

        if (technician.getRole() != Role.LAB_TECHNICIAN) {
            throw new BadRequestException(
                    "User is not a lab technician");
        }

        order.setTechnician(technician);

        order.setStatus(
                LabTestOrderStatus.CLAIMED
        );

        order.setCollectedAt(LocalDateTime.now());

        labTestOrderRepository.save(order);
    }

    @Transactional
    public void collectSample(Long orderId, String technicianEmail) {

        LabTestOrder order = labTestOrderRepository
                .findByIdForUpdate(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Lab test order not found"));

        if (order.getStatus() != LabTestOrderStatus.CLAIMED) {
            throw new BadRequestException(
                    "Order must be claimed before collecting sample");
        }

        if (!order.getTechnician().getEmail()
                .equals(technicianEmail)) {

            throw new BadRequestException(
                    "This order is assigned to another technician");
        }

        order.setStatus(LabTestOrderStatus.SAMPLE_COLLECTED);
        order.setCollectedAt(LocalDateTime.now());

        labTestOrderRepository.save(order);
    }

    @Transactional
    public void startProcessing(Long orderId, String technicianEmail) {

        LabTestOrder order = labTestOrderRepository
                .findByIdForUpdate(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Lab test order not found"));

        if (order.getStatus() != LabTestOrderStatus.SAMPLE_COLLECTED) {
            throw new BadRequestException(
                    "Sample must be collected before processing");
        }

        if (!order.getTechnician().getEmail()
                .equals(technicianEmail)) {

            throw new BadRequestException(
                    "This order is assigned to another technician");
        }

        order.setStatus(LabTestOrderStatus.PROCESSING);
        order.setProcessingStartedAt(LocalDateTime.now());

        labTestOrderRepository.save(order);
    }

    @Transactional
    public void completeOrder(
            Long orderId,
            String technicianEmail,
            CompleteLabTestRequest request) {

        LabTestOrder order = labTestOrderRepository
                .findByIdForUpdate(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Lab test order not found"));

        if (order.getStatus() != LabTestOrderStatus.PROCESSING) {
            throw new BadRequestException(
                    "Order must be in PROCESSING state");
        }

        if (!order.getTechnician().getEmail()
                .equals(technicianEmail)) {

            throw new BadRequestException(
                    "This order is assigned to another technician");
        }

        LabTestResult result = new LabTestResult();

        result.setOrder(order);
        result.setResult(request.getResult());
        result.setRemarks(request.getRemarks());
        result.setCreatedAt(LocalDateTime.now());

        labTestResultRepository.save(result);

        order.setStatus(LabTestOrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());

        labTestOrderRepository.save(order);
    }

    public List<PatientLabOrderResponse> getMyLabOrders(
            String email) {

        return labTestOrderRepository.findPatientOrders(email);
    }


    public PatientLabResultResponse getPatientResult(
            Long orderId,
            String email) {

        return labTestResultRepository
                .findPatientResult(orderId, email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Lab test result not found"));
    }
}