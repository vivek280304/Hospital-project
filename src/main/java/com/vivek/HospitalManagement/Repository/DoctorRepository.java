package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.DTO.Auth.Response.DoctorResponse;
import com.vivek.HospitalManagement.Entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor,Long> {

    Optional<Doctor> findByUserId(Long userId);

    @Query("""
        SELECT DISTINCT d
        FROM Doctor d
        JOIN d.schedules s
        WHERE s.dayOfWeek = :day
        AND (:specialization IS NULL OR
             LOWER(d.specialization) = LOWER(:specialization))
        """)
    List<Doctor> findAvailableDoctors(
            @Param("day") DayOfWeek day,
            @Param("specialization") String specialization
    );

    Optional<Doctor> findByUserEmail(String email);

    @Query("""
    SELECT new com.vivek.HospitalManagement.DTO.Auth.Response.DoctorResponse(
        d.id,
        u.name,
        d.specialization,
        d.experience
    )
    FROM Doctor d
    JOIN d.user u
    WHERE LOWER(d.specialization) = LOWER(:specialization)
    ORDER BY d.experience DESC
""")
    List<DoctorResponse> findDoctorsBySpecialization(
            @Param("specialization") String specialization
    );
}
