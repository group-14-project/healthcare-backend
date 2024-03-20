package com.example.server.patient;

import com.example.server.converter.PatientObjectConverter;
import com.example.server.dto.request.LoginUserRequest;
import com.example.server.dto.request.SignupPatientRequest;
import com.example.server.dto.request.VerifyEmailRequest;
import com.example.server.dto.response.PatientResponse;
import com.example.server.emailOtp.EmailSender;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patient")
@CrossOrigin
public class PatientController {
    private final PatientService patient;

    private final EmailSender emailSender;
    private final PatientObjectConverter converter;

    public PatientController(PatientService patient, PatientObjectConverter converter, EmailSender emailSender){
        this.patient = patient;
        this.converter = converter;
        this.emailSender = emailSender;
    }

    public static class PatientVerifiedException extends SecurityException{
        public PatientVerifiedException(){ super("Patient already Verified");}
    }

    @PostMapping("/login")
    ResponseEntity<PatientResponse> loginPatient(@RequestBody VerifyEmailRequest body){
        PatientEntity newPatient = patient.verifyPatient(
                body.getUser().getEmail(),
                body.getUser().getOtp()
        );
        return ResponseEntity.ok(converter.entityToResponse(newPatient));
    }

    @PostMapping("/signup")
    ResponseEntity<Void> registerPatient(@RequestBody VerifyEmailRequest body){
        PatientEntity newPatient = patient.verifyPatient(
                body.getUser().getEmail(),
                body.getUser().getOtp()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/loginotp")
    ResponseEntity<Void> loginPatientemail(@RequestBody LoginUserRequest body){
        PatientEntity currentPatient = patient.verifyPatient(
                body.getUser().getEmail(),
                body.getUser().getPassword()
        );
        String otp = emailSender.sendOtpEmail(
                body.getUser().getEmail(),
                currentPatient.getFirstName()
        );
        PatientEntity patient1 = patient.updateOtp(otp, currentPatient.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/signupotp")
    ResponseEntity<Void> sendEmail(@RequestBody SignupPatientRequest body){
        if(patient.checkPatientVerification(body.getPatient().getEmail())){
            throw new PatientVerifiedException();
        }
        String otp = emailSender.sendOtpEmail(
                body.getPatient().getEmail(),
                body.getPatient().getFirstName()
        );
        if(patient.checkPatient(body.getPatient().getEmail())){
            PatientEntity currentPatient = patient.updateOtp(otp, body.getPatient().getEmail());
        }else {
            PatientEntity currentPatient = patient.registerNewPatient(
                    body.getPatient().getFirstName(),
                    body.getPatient().getLastName(),
                    body.getPatient().getPassword(),
                    body.getPatient().getEmail(),
                    otp
            );
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
