package com.example.server.patient;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PatientService {
    private final PatientRepository patientRepo;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public static class PatientConflictException extends SecurityException{
        public PatientConflictException(){
            super("Patient Email Already Exists");
        }
    }

    public static class PatientNotFoundException extends SecurityException{
        public PatientNotFoundException(){
            super("Patient not found");
        }
    }

    public static class PatientAlreadyRegisteredException extends SecurityException{
        public PatientAlreadyRegisteredException(){
            super("Patient not found");
        }
    }

    public static class OtpNotVerifiedException extends SecurityException{
        public OtpNotVerifiedException(){ super("OTP not verified"); }
    }

    public static class InvalidPasswordException extends SecurityException{
        public InvalidPasswordException(){
            super("Incorrect Password");
        }
    }

    @Bean
    public static BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    public PatientService(PatientRepository patientRepo, BCryptPasswordEncoder bCryptPasswordEncoder){
        this.patientRepo = patientRepo;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    //This is to register a new patient
    public PatientEntity registerNewPatient(String firstName, String lastName, String password, String email, String otp){
        PatientEntity patient = patientRepo.findPatientEntitiesByEmail(email);
        //TODO: check if email is already there and not verified.
        if(patient != null){
            throw new PatientConflictException();
        }

        PatientEntity newPatient = new PatientEntity();
        newPatient.setFirstName(firstName);
        newPatient.setLastName(lastName);
        newPatient.setEmail(email);
        newPatient.setPassword(bCryptPasswordEncoder.encode(password));
        newPatient.setOtp(otp);
        newPatient.setOtpGeneratedTime(LocalDateTime.now());

        return patientRepo.save(newPatient);
    }

    public PatientEntity verifyPatient(String email, String otp){
        PatientEntity patient = patientRepo.findPatientEntitiesByEmail(email);
        if(patient==null){
            throw new PatientNotFoundException();
        }
        else if(patient.getOtp().equals(otp) && Duration.between(patient.getOtpGeneratedTime(),
                LocalDateTime.now()).getSeconds()<(2*60)) {
            patient.setEmailVerify(true);
            patientRepo.save(patient);
        }
        else{
            throw new OtpNotVerifiedException();
        }
        return patient;
    }

    public PatientEntity updateOtp(String otp, String email){
        PatientEntity patient = patientRepo.findPatientEntitiesByEmail(email);
        if(patient==null){
            throw new PatientNotFoundException();
        }
        patient.setOtp(otp);
        patient.setOtpGeneratedTime(LocalDateTime.now());
        return patientRepo.save(patient);
    }

    public boolean checkPatientVerification(String email){
        PatientEntity patient = patientRepo.findPatientEntitiesByEmail(email);
        if(patient==null) return false;
        return patient.isEmailVerify();
    }

    public boolean checkPatient(String email){
        PatientEntity patient = patientRepo.findPatientEntitiesByEmail(email);
        return patient != null;
    }
}
