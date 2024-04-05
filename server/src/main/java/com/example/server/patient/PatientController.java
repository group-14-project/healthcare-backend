package com.example.server.patient;

import com.example.server.connection.ConnectionEntity;
import com.example.server.connection.ConnectionService;
import com.example.server.consultation.ConsultationService;
import com.example.server.converter.PatientObjectConverter;
import com.example.server.dto.request.LoginUserRequest;
import com.example.server.dto.request.PatientDetailsRequest;
import com.example.server.dto.request.SignupPatientRequest;
import com.example.server.dto.request.VerifyEmailRequest;
import com.example.server.dto.response.AppointmentDetailsDto;
import com.example.server.dto.response.PatientResponse;
import com.example.server.dto.response.PatientUpdateDetails;
import com.example.server.emailOtpPassword.EmailSender;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patient")
@CrossOrigin
public class   PatientController {
    private final PatientService patient;

    private final EmailSender emailSender;

    //private final PatientObjectConverter converter;

    private final ConnectionService connection;

    private final ConsultationService consultation;

    public PatientController(PatientService patient, PatientObjectConverter converter, EmailSender emailSender, ConnectionService connection, ConsultationService consultation){
        this.patient = patient;
       // this.converter = converter;
        this.emailSender = emailSender;
        this.connection = connection;
        this.consultation = consultation;
    }

    public static class PatientVerifiedException extends SecurityException{
        public PatientVerifiedException(){ super("Patient already Verified");}
    }

    public static class UnexpectedErrorException extends SecurityException{
        public UnexpectedErrorException(){ super("Unexpected Error Occured");}
    }

    @PostMapping("/login")
    ResponseEntity<PatientResponse> loginPatient(@RequestBody VerifyEmailRequest body){
        PatientEntity newPatient = patient.verifyPatient(
                body.getUser().getEmail(),
                body.getUser().getOtp()
        );

        List<ConnectionEntity> connectionEntities = connection.findAllConnections(newPatient);
        List<AppointmentDetailsDto> pastAppointmentDetails = consultation.findPastAppointments(connectionEntities);
        List<AppointmentDetailsDto> futureAppointmentDetails = consultation.findFutureAppointments(connectionEntities);

        PatientResponse patientResponse = new PatientResponse(newPatient.getId(),
                newPatient.getEmail(), newPatient.getFirstName(), newPatient.getLastName(), newPatient.getHeight(), newPatient.getWeight(), newPatient.getBloodGroup(),
                newPatient.getGender(), newPatient.isFirstTimeLogin(), pastAppointmentDetails, futureAppointmentDetails);

        return ResponseEntity.ok(patientResponse);
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
    ResponseEntity<Void> loginPatientEmail(@RequestBody LoginUserRequest body){
        if(!patient.checkPatient(body.getUser().getEmail())){
            throw new PatientService.PatientNotFoundException();
        }
        PatientEntity currentPatient = patient.patientDetails(body.getUser().getEmail());
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
            patient.passwordChange(body.getPatient().getPassword(), body.getPatient().getEmail());
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

    @PostMapping("/verifyOtp")
    ResponseEntity<Void> verifyOtp(String email, String name){
        if(!patient.checkPatient(email)){
            throw new UnexpectedErrorException();
        }
        String otp = emailSender.sendOtpEmail(email, name);
        PatientEntity newPatient = patient.updateOtp(otp, email);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PostMapping("/detailsAdd")
    ResponseEntity<PatientDetailsRequest> DetailsAdd(@RequestBody PatientDetailsRequest patientDto)
    {
        PatientDetailsRequest details=this.patient.DetailsAdd(patientDto);
        return new ResponseEntity<>(details,HttpStatus.OK);
    }

    @PutMapping("/changePassword")
    ResponseEntity<Void> changePassword(@RequestBody LoginUserRequest body){
        patient.passwordChange(
                body.getUser().getPassword(),
                body.getUser().getEmail()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/updateDetail")
    public ResponseEntity<PatientUpdateDetails> updateDetail(@RequestBody PatientDetailsRequest body)
    {
        if(!patient.checkPatient(body.getEmail()))
        {
            throw new PatientService.PatientNotFoundException();
        }
        PatientEntity newPatient = patient.updateDetails(body);
        List<ConnectionEntity> connectionEntities = connection.findAllConnections(newPatient);
        List<AppointmentDetailsDto> pastAppointmentDetails = consultation.findPastAppointments(connectionEntities);
        List<AppointmentDetailsDto> futureAppointmentDetails = consultation.findFutureAppointments(connectionEntities);

        PatientUpdateDetails patientResponse = new PatientUpdateDetails(
                newPatient.getEmail(), newPatient.getFirstName(), newPatient.getLastName(), newPatient.getHeight(), newPatient.getWeight(), newPatient.getBloodGroup(),
                newPatient.getGender(), newPatient.isFirstTimeLogin(), pastAppointmentDetails, futureAppointmentDetails);

        return ResponseEntity.ok(patientResponse);
    }
}
