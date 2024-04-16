package com.example.server.emailOtpPassword;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
@Getter
public class OtpUtil {
    public String generateOtp() {
        int randomNumber = ThreadLocalRandom.current().nextInt(100000, 1000000); // Generate random number between 100000 and 999999
        return String.format("%06d", randomNumber);
    }
}
