package com.vivek.HospitalManagement.Entity;


import com.vivek.HospitalManagement.Enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDateTime;


@Setter
@Getter
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    @NotBlank
    private String name;

    @NotBlank
    private String password;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean enabled ;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    @Column(nullable = false)
    private boolean accountNonLocked = true;

    @Column
    private LocalDateTime lockedUntil;

    @Column(nullable = false)
    private int registrationOtpRequests = 0;

    @Column
    private LocalDateTime registrationOtpWindowStart;

    @Column(nullable = false)
    private int passwordResetOtpRequests = 0;

    @Column
    private LocalDateTime passwordResetOtpWindowStart;


    public User() {
    }

    public User(String name, @NonNull String password, @NonNull String email, Role role) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.role = role;
        this.enabled = true;
    }


}
