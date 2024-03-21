package com.example.server.hospital;

import com.example.server.dto.request.LoginUserRequest;
import com.example.server.patient.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class HospitalService {
    private final HospitalRepository hospitalRepository;

    private final PasswordEncoder passwordEncoder;



    @Autowired
    private final BCryptPasswordEncoder bCryptPasswordEncoder;



    public static class HospitalNotFoundException extends SecurityException{
        public HospitalNotFoundException(){
            super("Patient not found");
        }
    }


    public HospitalService(HospitalRepository hospitalRepository, PasswordEncoder passwordEncoder, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.hospitalRepository = hospitalRepository;
        this.passwordEncoder = passwordEncoder;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
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


    public void updatePassword(LoginUserRequest data)
    {
        HospitalEntity hospital = this.hospitalRepository.findByEmail(data.getUser().getEmail());
        hospital.setPassword(bCryptPasswordEncoder.encode(data.getUser().getPassword()));
        this.hospitalRepository.save(hospital);
    }


    public boolean checkHospital(String email)
    {
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
