package com.vivek.HospitalManagement.DTO.Auth.Response;

import com.vivek.HospitalManagement.Enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminUserResponse {

    Long id;
    String name;
    String email;
    Role role;
    private boolean accountNonLocked;

}
