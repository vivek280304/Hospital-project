package com.vivek.HospitalManagement.Security;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class InitialPassword {

    public String generateInitialPassword() {

        String characters =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                        "abcdefghijklmnopqrstuvwxyz" +
                        "0123456789"+"@$&";

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            password.append(
                    characters.charAt(random.nextInt(characters.length()))
            );
        }

        return password.toString();
    }
}
