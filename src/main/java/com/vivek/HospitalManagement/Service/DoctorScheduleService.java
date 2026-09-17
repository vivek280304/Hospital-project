package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.DTO.Auth.Request.DoctorScheduleRequest;
import com.vivek.HospitalManagement.Entity.Doctor;
import com.vivek.HospitalManagement.Entity.DoctorSchedule;
import com.vivek.HospitalManagement.Repository.DoctorRepository;
import com.vivek.HospitalManagement.Repository.DoctorScheduleRepository;
import org.springframework.stereotype.Service;

@Service
public class DoctorScheduleService {

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final DoctorRepository doctorRepository;

    public DoctorScheduleService(DoctorScheduleRepository doctorScheduleRepository, DoctorRepository doctorRepository) {
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.doctorRepository = doctorRepository;
    }

    public void createSchedule(DoctorScheduleRequest request) {

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found"));

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new RuntimeException(
                    "Start time must be before end time"
            );
        }

        DoctorSchedule schedule = new DoctorSchedule();

        schedule.setDoctor(doctor);
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());

        doctorScheduleRepository.save(schedule);
    }

}
