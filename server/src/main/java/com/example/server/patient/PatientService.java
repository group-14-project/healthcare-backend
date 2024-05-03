package com.example.server.patient;

import com.example.server.common.CommonService;
import com.example.server.doctor.DoctorService;
import com.example.server.dto.request.PatientDetailsRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class PatientService {
    private final PatientRepository patientRepo;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final CommonService commonService;

    @Autowired
    private ModelMapper modelMapper;

    public PatientEntity checkJWT(String email, String jwtToken) {
        PatientEntity patient = patientRepo.findPatientEntitiesByEmail(email);
        if(patient==null || !Objects.equals(patient.getJwtToken(), jwtToken)){
            return null;
        }
        return checkAccessTime(patient.getEmail());
    }

    public PatientEntity checkAccessTime(String email){
        PatientEntity patient = patientRepo.findPatientEntitiesByEmail(email);
        if (patient.getLastAccessTime().plusHours(2).isBefore(LocalDateTime.now())) {
            return null;
        }
        return patient;
    }

    public void deleteAccount(String email)
    {
        PatientEntity patient = patientRepo.findPatientEntitiesByEmail(email);
        patient.setDeleteEntry(true);
        patient.setDeletionTime(LocalDate.now());
        patientRepo.save(patient);
    }


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


    @Bean
    public static BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }


    public PatientService(PatientRepository patientRepo, BCryptPasswordEncoder bCryptPasswordEncoder, DoctorService doctorService, CommonService commonService){
        this.patientRepo = patientRepo;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.commonService = commonService;
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
        newPatient.setFirstTimeLogin(false);
        newPatient.setOtpGeneratedTime(LocalDateTime.now());
        newPatient.setRole("ROLE_patient");

        return patientRepo.save(newPatient);
    }

    public PatientEntity verifyPatient(String email, String otp){
        PatientEntity patient = patientRepo.findPatientEntitiesByEmail(email);
        if(patient==null){
            return null;
        }
        else if(patient.getOtp().equals(otp) && Duration.between(patient.getOtpGeneratedTime(),
                LocalDateTime.now()).getSeconds()<(2*60)) {
            patient.setEmailVerify(true);
            patientRepo.save(patient);
        }
        else{
            return null;
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

    public void passwordChange(String password, String email){
        PatientEntity patient = patientRepo.findPatientEntitiesByEmail(email);
        patient.setPassword(bCryptPasswordEncoder.encode(password));
        patientRepo.save(patient);
    }

    public PatientEntity patientDetails(String email){
        PatientEntity patient = patientRepo.findPatientEntitiesByEmail(email);
        if(patient==null){
            throw new PatientNotFoundException();
        }
        return patient;
    }

    public  PatientEntity updateDetails(PatientDetailsRequest body, String email)
    {
        PatientEntity patient=patientRepo.findPatientEntitiesByEmail(email);
        patient.setPhoneNumber(commonService.encrypt(body.getPhoneNumber()));
        patient.setWeight(commonService.encrypt(body.getWeight()));
        patient.setHeight(commonService.encrypt(body.getHeight()));
        patient.setBloodGroup(commonService.encrypt(body.getBloodGroup()));
        patient.setGender(body.getGender());
        patient.setAddress(commonService.encrypt(body.getAddress()));
        patient.setPinCode(commonService.encrypt(body.getPinCode()));
        patient.setFirstTimeLogin(true);
        patient.setLastAccessTime(LocalDateTime.now());
        return patientRepo.save(patient);
    }

    public void setJwtToken(String jwtToken, String email) {
        PatientEntity patient = patientRepo.findPatientEntitiesByEmail(email);
        patient.setJwtToken(jwtToken);
        patientRepo.save(patient);
    }

    public void setLastAccessTime(String email) {
        PatientEntity patient = patientRepo.findPatientEntitiesByEmail(email);
        patient.setLastAccessTime(LocalDateTime.now());
        patientRepo.save(patient);
    }

    public PatientEntity checkPassword(String email, String password) {
        PatientEntity patient = patientRepo.findPatientEntitiesByEmail(email);
        if(patient==null){
            return null;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, patient.getPassword())) {
            return null;
        }
        return patient;
    }

    public void expireJWTfromTable(String email) {
        PatientEntity patient = patientRepo.findPatientEntitiesByEmail(email);
        patient.setJwtToken("Expired");
    }

}
