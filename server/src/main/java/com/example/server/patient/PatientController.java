package com.example.server.patient;

import com.example.server.converter.PatientObjectConverter;
import com.example.server.dto.request.LoginUserRequest;
import com.example.server.dto.request.SignupPatientRequest;
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

    public static class UnexpectedError extends SecurityException{
        public UnexpectedError(){ super("Some unexpected error occured");}
    }
    @PostMapping("/signup")
    ResponseEntity<Void> registerPatient(@RequestBody SignupPatientRequest body){
        PatientEntity newPatient = patient.registerNewPatient(
                body.getPatient().getName(),
                body.getPatient().getEmail(),
                body.getPatient().getPassword()
        );
        if(newPatient==null){
            throw new UnexpectedError();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    ResponseEntity<PatientResponse> loginPatient(@RequestBody LoginUserRequest body){
        PatientEntity currentPatient = patient.verifyPatient(
                body.getUser().getEmail(),
                body.getUser().getPassword()
        );
        return ResponseEntity.ok(converter.entityToResponse(currentPatient));
    }

    @PostMapping("/email")
    ResponseEntity<Void> sendEmail(@RequestBody SignupPatientRequest body){
        emailSender.sendOtpEmail(
                body.getPatient().getEmail(),
                body.getPatient().getName()
        );
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
