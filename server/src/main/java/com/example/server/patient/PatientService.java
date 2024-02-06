package com.example.server.patient;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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

    public PatientEntity registerNewPatient(String name, String email, String password){
        PatientEntity patient = patientRepo.findPatientEntitiesByEmail(email);
        if(patient != null){
            throw new PatientConflictException();
        }
        PatientEntity newPatient = new PatientEntity();
        newPatient.setName(name);
        newPatient.setEmail(email);
        newPatient.setPassword(bCryptPasswordEncoder.encode(password));

        return patientRepo.save(newPatient);
    }

    public PatientEntity verifyPatient(String email, String password){
        PatientEntity patient = patientRepo.findPatientEntitiesByEmail(email);
        if(patient==null){
            throw new PatientNotFoundException();
        }
        if(!bCryptPasswordEncoder.matches(password, patient.getPassword())){
            throw new InvalidPasswordException();
        }
        return patient;
    }
}
