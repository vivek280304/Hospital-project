package com.vivek.HospitalManagement.Service.Payment;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CashfreeWebhookService {

    @Value("${cashfree.client-secret}")
    private String clientSecret;

    public boolean verifySignature(
            String signature,
            String timestamp,
            String rawBody) {

        try {

            String signedPayload = timestamp + rawBody;

            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(
                            clientSecret.getBytes(StandardCharsets.UTF_8),
                            "HmacSHA256"
                    );

            mac.init(secretKeySpec);

            byte[] hash =
                    mac.doFinal(
                            signedPayload.getBytes(StandardCharsets.UTF_8)
                    );

            String generatedSignature =
                    Base64.getEncoder().encodeToString(hash);

            return generatedSignature.equals(signature);

        } catch (Exception e) {
            return false;
        }
    }
}