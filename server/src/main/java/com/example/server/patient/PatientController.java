package com.example.server.patient;

import com.example.server.connection.ConnectionEntity;
import com.example.server.connection.ConnectionService;
import com.example.server.consent.ConsentEntity;
import com.example.server.consent.ConsentService;
import com.example.server.consultation.ConsultationService;
import com.example.server.common.PatientObjectConverter;
import com.example.server.dto.request.*;
import com.example.server.dto.response.*;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.errorOrSuccessMessageResponse.ErrorMessage;
import com.example.server.errorOrSuccessMessageResponse.SuccessMessage;
import com.example.server.jwtToken.JWTService;
import com.example.server.jwtToken.JWTTokenReCheck;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
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

    private final JWTService jwtService;

    private final JWTTokenReCheck jwtTokenReCheck;
    private final ConsentService consent;

    private final ConnectionService connection;

    private final ConsultationService consultation;

    public PatientController(PatientService patient, EmailSender emailSender, JWTService jwtService, JWTTokenReCheck jwtTokenReCheck, ConsentService consent, ConnectionService connection, ConsultationService consultation){
        this.patient = patient;
        this.emailSender = emailSender;
        this.jwtService = jwtService;
        this.jwtTokenReCheck = jwtTokenReCheck;
        this.consent = consent;
        this.connection = connection;
        this.consultation = consultation;
    }


    public static class UnexpectedErrorException extends SecurityException{
        public UnexpectedErrorException(){ super("Unexpected Error Occurred");}
    }

    //JWT done
    @PostMapping("/login")
    ResponseEntity<?> loginPatient(@RequestBody VerifyEmailRequest body){
        PatientEntity newPatient = patient.verifyPatient(
                body.getUser().getEmail(),
                body.getUser().getOtp()
        );

        if(newPatient==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Wrong OTP or email ID");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        List<ConnectionEntity> connectionEntities = connection.findAllConnections(newPatient);
        List<AppointmentDetailsDto> pastAppointmentDetails = consultation.findPastAppointments(connectionEntities);
        List<AppointmentDetailsDto> futureAppointmentDetails = consultation.findFutureAppointments(connectionEntities);

        PatientResponse patientResponse = new PatientResponse(newPatient.getId(),
                newPatient.getEmail(), newPatient.getFirstName(), newPatient.getLastName(), newPatient.getHeight(), newPatient.getWeight(), newPatient.getBloodGroup(),
                newPatient.getGender(), newPatient.isFirstTimeLogin(), pastAppointmentDetails, futureAppointmentDetails);

        String jwtToken = jwtService.createJwt(newPatient.getEmail(), newPatient.getRole());
        patient.setJwtToken(jwtToken, newPatient.getEmail());
        patient.setLastAccessTime(newPatient.getEmail());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        return ResponseEntity.ok().headers(headers).body(patientResponse);
    }

    @PostMapping("/signup")
    ResponseEntity<?> registerPatient(@RequestBody VerifyEmailRequest body){
        PatientEntity newPatient = patient.verifyPatient(
                body.getUser().getEmail(),
                body.getUser().getOtp()
        );
        if(newPatient==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Wrong OTP or email ID");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("OTP has been verified. Please Login");
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping("/loginotp")
    ResponseEntity<?> loginPatientEmail(@RequestBody LoginUserRequest body){
        PatientEntity currentPatient = patient.checkPassword(body.getUser().getEmail(), body.getUser().getPassword());
        if(currentPatient==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Wrong Email or Wrong Password");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        String otp = emailSender.sendOtpEmail(
                body.getUser().getEmail(),
                currentPatient.getFirstName()
        );
        PatientEntity patient1 = patient.updateOtp(otp, currentPatient.getEmail());

        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("Password has been verified. An OTP has been sent to the email");
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping("/signupotp")
    ResponseEntity<?> sendEmailForSignup(@RequestBody SignupPatientRequest body){
        if(patient.checkPatientVerification(body.getPatient().getEmail())){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("User has already signed up");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
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
        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("An OTP has been sent to the email. Verify the OTP");
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping("/resendOtp")
    ResponseEntity<?> resendOtp(@RequestBody EmailRequest email){
        PatientEntity patientEntity = patient.patientDetails(email.getEmail());
        if(patientEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Wrong Email ID");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        String otp = emailSender.sendOtpEmail(email.getEmail(), patientEntity.getFirstName());
        PatientEntity newPatient = patient.updateOtp(otp, email.getEmail());
        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("An OTP has been sent to the email. Verify the OTP");
        return ResponseEntity.ok(successMessage);
    }


    //JWT Not Done
    @PutMapping("/changePassword")
    ResponseEntity<Void> changePassword(@RequestBody LoginUserRequest body){
        patient.passwordChange(
                body.getUser().getPassword(),
                body.getUser().getEmail()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //JWT Done
    @PutMapping("/updateDetail")
    public ResponseEntity<?> updateDetail(@RequestBody PatientDetailsRequest body, HttpServletRequest request)
    {
        PatientEntity patientEntity = jwtTokenReCheck.checkJWTAndSessionPatient(request);
        if(patientEntity==null)
        {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        PatientEntity newPatient = patient.updateDetails(body, patientEntity.getEmail());
        List<ConnectionEntity> connectionEntities = connection.findAllConnections(newPatient);
        List<AppointmentDetailsDto> pastAppointmentDetails = consultation.findPastAppointments(connectionEntities);
        List<AppointmentDetailsDto> futureAppointmentDetails = consultation.findFutureAppointments(connectionEntities);

        PatientUpdateDetails patientResponse = new PatientUpdateDetails(
                newPatient.getEmail(), newPatient.getFirstName(), newPatient.getLastName(), newPatient.getHeight(), newPatient.getWeight(), newPatient.getBloodGroup(),
                newPatient.getGender(), newPatient.isFirstTimeLogin(), pastAppointmentDetails, futureAppointmentDetails);


        return ResponseEntity.ok(patientResponse);
    }

    //JWT DONE
    @GetMapping("/viewConsents")
    public ResponseEntity<?> viewConsentByPatient(HttpServletRequest request){
        PatientEntity patientEntity = jwtTokenReCheck.checkJWTAndSessionPatient(request);
        if(patientEntity==null)
        {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        List<ConnectionEntity> connectionEntities = connection.findAllConnections(patientEntity);
        List<ConsentEntity> consentEntities = consent.findByConnect(connectionEntities);

        List<ViewConsentResponse> viewConsentResponses = consentEntities.stream().map(
                consentEntity -> new ViewConsentResponse(
                        consentEntity.getConnect().getPatient().getFirstName(),
                        consentEntity.getConnect().getPatient().getLastName(),
                        consentEntity.getConnect().getDoctor().getFirstName(),
                        consentEntity.getConnect().getDoctor().getLastName(),
                        consentEntity.getConnect().getDoctor().getHospitalSpecializationhead().getHeadDoctor().getFirstName(),
                        consentEntity.getConnect().getDoctor().getHospitalSpecializationhead().getHeadDoctor().getLastName(),
                        consentEntity.getNewDoctor().getFirstName(),
                        consentEntity.getNewDoctor().getLastName(),
                        consentEntity.isPatientConsent(),
                        consentEntity.isSeniorDoctorConsent(),
                        consentEntity.getLocalDate()
                )
        ).toList();

        return ResponseEntity.ok(viewConsentResponses);
    }
}
