package com.vivek.HospitalManagement.Security;

import com.vivek.HospitalManagement.Entity.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final com.vivek.HospitalManagement.Entity.User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name()
                ));
    }

    @Override
    public  String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }



    @Override
    public boolean isAccountNonLocked() {

        if (!user.isAccountNonLocked()) {

            LocalDateTime lockedUntil =
                    user.getLockedUntil();

            if (lockedUntil != null &&
                    LocalDateTime.now().isAfter(lockedUntil)) {

                user.setAccountNonLocked(true);
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);

                return true;
            }

            return false;
        }

        return true;
    }
}
