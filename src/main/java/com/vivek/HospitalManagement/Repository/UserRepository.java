package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vivek.HospitalManagement.Enums.Role;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(Role role);

}
