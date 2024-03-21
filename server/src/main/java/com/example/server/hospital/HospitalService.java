package com.example.server.hospital;

import com.example.server.patient.PatientService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class HospitalService {
    private final HospitalRepository hospitalRepository;

    private final PasswordEncoder passwordEncoder;

    public static class HospitalNotFoundException extends SecurityException{
        public HospitalNotFoundException(){
            super("Patient not found");
        }
    }


    public HospitalService(HospitalRepository hospitalRepository, PasswordEncoder passwordEncoder) {
        this.hospitalRepository = hospitalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public HospitalEntity verifyHospital(String email, String otp){
        HospitalEntity hospital = hospitalRepository.findByEmail(email);
        if(hospital==null){
            throw new HospitalNotFoundException();
        }
        else if(hospital.getOtp().equals(otp) && Duration.between(hospital.getOtpGeneratedTime(),
                LocalDateTime.now()).getSeconds()<(2*60)) {
            hospital.setEmailVerify(true);
            hospitalRepository.save(hospital);
        }
        else{
            throw new PatientService.OtpNotVerifiedException();
        }
        return hospital;
    }

    public boolean checkHospital(String email){
        HospitalEntity hospital = hospitalRepository.findByEmail(email);
        return hospital != null;
    }

    public HospitalEntity hospitalDetails(String email){
        HospitalEntity hospital = hospitalRepository.findByEmail(email);
        if(hospital==null){
            throw new PatientService.PatientNotFoundException();
        }
        return hospital;
    }

    public HospitalEntity updateOtp(String otp, String email){
        HospitalEntity hospital = hospitalRepository.findByEmail(email);
        if(hospital==null){
            throw new PatientService.PatientNotFoundException();
        }
        hospital.setOtp(otp);
        hospital.setOtpGeneratedTime(LocalDateTime.now());
        return hospitalRepository.save(hospital);
    }




    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }
}
