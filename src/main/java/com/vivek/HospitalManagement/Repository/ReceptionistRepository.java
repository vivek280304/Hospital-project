package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.Receptionist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReceptionistRepository extends JpaRepository<Receptionist,Long> {

    Optional<Receptionist> findByUserEmail(String email);



    Optional<Receptionist> findByUserId (Long userId);
}
