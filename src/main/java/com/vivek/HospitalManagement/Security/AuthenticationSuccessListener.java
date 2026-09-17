package com.vivek.HospitalManagement.Security;

import com.vivek.HospitalManagement.Repository.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener
        implements ApplicationListener<AuthenticationSuccessEvent> {

    private final UserRepository userRepository;

    public AuthenticationSuccessListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onApplicationEvent(
            AuthenticationSuccessEvent event) {

        String email = event.getAuthentication().getName();

        userRepository.findByEmail(email).ifPresent(user -> {

            if (user.getFailedLoginAttempts() > 0) {

                user.setFailedLoginAttempts(0);

                userRepository.save(user);
            }
        });
    }
}