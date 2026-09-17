package com.vivek.HospitalManagement.Security;

import com.vivek.HospitalManagement.Repository.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuthenticationFailureListener
        implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private final UserRepository userRepository;

    public AuthenticationFailureListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onApplicationEvent(
            AuthenticationFailureBadCredentialsEvent event) {

        String email = event.getAuthentication().getName();

        userRepository.findByEmail(email).ifPresent(user -> {

            int attempts = user.getFailedLoginAttempts() + 1;

            user.setFailedLoginAttempts(attempts);

            if (attempts >= 5) {
                user.setAccountNonLocked(false);
                user.setLockedUntil(
                        LocalDateTime.now().plusMinutes(15)
                );
            }

            userRepository.save(user);
        });
    }
}