package com.vivek.HospitalManagement.Security;

import com.vivek.HospitalManagement.Entity.User;
import com.vivek.HospitalManagement.Repository.UserRepository;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CustomUserDetailsService implements UserDetailsService {


    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found"
                        ));

        // Check temporary lock
        if (!user.isAccountNonLocked()) {

            if (user.getLockedUntil() != null &&
                    LocalDateTime.now()
                            .isAfter(user.getLockedUntil())) {

                // Lock expired → unlock account
                user.setAccountNonLocked(true);
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);

                userRepository.save(user);

            } else {
                // Still locked
                throw new LockedException(
                        "Account is temporarily locked"
                );
            }
        }

        return new CustomUserDetails(user);
    }
}
