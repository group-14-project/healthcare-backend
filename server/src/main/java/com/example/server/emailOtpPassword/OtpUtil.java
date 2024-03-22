package com.example.server.emailOtpPassword;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
@Getter
public class OtpUtil {
    public String generateOtp() {
        int randomNumber = ThreadLocalRandom.current().nextInt(1000000);
        return String.format("%04d", randomNumber);
    }
}
