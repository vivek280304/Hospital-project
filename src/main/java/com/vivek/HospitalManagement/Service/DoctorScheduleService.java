package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.DTO.Auth.Request.DoctorScheduleRequest;
import com.vivek.HospitalManagement.Entity.Doctor;
import com.vivek.HospitalManagement.Entity.DoctorSchedule;
import com.vivek.HospitalManagement.Exceptions.BadRequestException;
import com.vivek.HospitalManagement.Exceptions.ResourceNotFoundException;
import com.vivek.HospitalManagement.Repository.DoctorRepository;
import com.vivek.HospitalManagement.Repository.DoctorScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class DoctorScheduleService {

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final DoctorRepository doctorRepository;

    public DoctorScheduleService(DoctorScheduleRepository doctorScheduleRepository, DoctorRepository doctorRepository) {
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public void createSchedule(DoctorScheduleRequest request) {

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor not found"));

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException(
                    "Start time must be before end time"
            );
        }

        int duration = request.getSlotDuration();

        if (duration <= 0) {
            throw new BadRequestException(
                    "Slot duration must be greater than 0"
            );
        }

        long totalMinutes = Duration.between(
                request.getStartTime(),
                request.getEndTime()
        ).toMinutes();

        if (duration > totalMinutes) {
            throw new BadRequestException(
                    "Slot duration cannot exceed schedule duration"
            );
        }
        DoctorSchedule schedule = new DoctorSchedule();

        schedule.setDoctor(doctor);
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setSlotDuration(request.getSlotDuration());

        doctorScheduleRepository.save(schedule);
    }

}
