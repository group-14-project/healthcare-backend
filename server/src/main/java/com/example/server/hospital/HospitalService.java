package com.example.server.hospital;
import com.example.server.dto.request.LoginUserRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class HospitalService {
    private final HospitalRepository hospitalRepository;

    private final PasswordEncoder passwordEncoder;


    public HospitalService(HospitalRepository hospitalRepository, PasswordEncoder passwordEncoder) {
        this.hospitalRepository = hospitalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void Authenticate(LoginUserRequest loginUserRequest)
    {
        String hospitalEmail = loginUserRequest.getUser().getEmail();
        try {
            HospitalEntity hospital = hospitalRepository.findByEmail(hospitalEmail)
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

            String userPassword = loginUserRequest.getUser().getPassword();
            if (!passwordEncoder.matches(userPassword, hospital.getPassword())) {
                throw new InvalidCredentialsException("Invalid email or password");
            }
            // Authentication successful
            System.out.println("Authentication successful for user: " + hospitalEmail);
        } catch (InvalidCredentialsException e) {
            System.out.println("Invalid credentials for user: " + hospitalEmail);
            throw e; // Rethrow the exception for controller to handle
        }
    }



    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }
}
