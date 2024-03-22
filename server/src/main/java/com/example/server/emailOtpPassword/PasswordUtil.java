package com.example.server.emailOtpPassword;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Getter
public class PasswordUtil {
    public String generateRandomPassword()
    {
        int passwordLength = 5;
        StringBuilder sb = new StringBuilder(passwordLength);
        Random random = new Random();
        String allowedCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < passwordLength; i++)
        {
            int randomIndex = random.nextInt(allowedCharacters.length());
            sb.append(allowedCharacters.charAt(randomIndex));
        }
        return sb.toString();
    }
}
