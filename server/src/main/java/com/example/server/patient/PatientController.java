package com.example.server.patient;

import com.example.server.converter.PatientObjectConverter;
import com.example.server.dto.request.LoginUserRequest;
import com.example.server.dto.request.SignupPatientRequest;
import com.example.server.dto.response.PatientResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/patient")
public class PatientController {
    private final PatientService patient;
    private final PatientObjectConverter converter;

    public PatientController(PatientService patient, PatientObjectConverter converter){
        this.patient = patient;
        this.converter = converter;
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
}
