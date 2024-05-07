package com.example.server.doctor;

import com.example.server.connection.ConnectionEntity;
import com.example.server.connection.ConnectionService;
import com.example.server.consent.ConsentEntity;
import com.example.server.consent.ConsentService;
import com.example.server.dto.request.GiveConsentRequest;
import com.example.server.dto.response.SeniorDoctorViewConsentResponse;
import com.example.server.dto.response.ViewConsentResponse;
import com.example.server.errorOrSuccessMessageResponse.ErrorMessage;
import com.example.server.errorOrSuccessMessageResponse.SuccessMessage;
import com.example.server.jwtToken.JWTService;
import com.example.server.jwtToken.JWTTokenReCheck;
import com.example.server.patient.PatientEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.filters.ExpiresFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/senior_doctor")
@CrossOrigin
public class SeniorDoctorController {

    private final DoctorService doctor;
    private final JWTTokenReCheck jwtTokenReCheck;
    private final ConnectionService connection;
    private final ConsentService consent;

    public SeniorDoctorController(DoctorService doctor, JWTTokenReCheck jwtTokenReCheck, ConnectionService connection, ConsentService consent) {
        this.doctor = doctor;
        this.jwtTokenReCheck = jwtTokenReCheck;
        this.connection = connection;
        this.consent = consent;
    }

    //TODO: get all doctors from this list and get all the connections from here
    @GetMapping("/viewConsents")
    public ResponseEntity<?> viewConsentBySrDoctor(HttpServletRequest request){
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionSeniorDoctor(request);
        if(doctorEntity==null)
        {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        List<DoctorEntity> doctorEntities = doctor.getDoctorsUnder(doctorEntity);
        List<ConnectionEntity> connectionEntities = connection.findAllConnectionsByDoctorList(doctorEntities);
        List<ConsentEntity> consentEntities = consent.findByConnect(connectionEntities);

        List<ConsentEntity> pendingConsentEntities = consent.findPendingConsentBySrDoctor(consentEntities);
        List<ConsentEntity> approvedConsentEntities = consent.findApprovedConsentBySrDoctor(consentEntities);

        List<ViewConsentResponse> viewPendingConsentResponses = pendingConsentEntities.stream().map(
                consentEntity -> new ViewConsentResponse(
                        consentEntity.getId(),
                        consentEntity.getConnect().getPatient().getFirstName(),
                        consentEntity.getConnect().getPatient().getLastName(),
                        consentEntity.getConnect().getDoctor().getFirstName(),
                        consentEntity.getConnect().getDoctor().getLastName(),
                        consentEntity.getConnect().getDoctor().getHospitalSpecialization().getHeadDoctor().getFirstName(),
                        consentEntity.getConnect().getDoctor().getHospitalSpecialization().getHeadDoctor().getLastName(),
                        consentEntity.getNewDoctor().getFirstName(),
                        consentEntity.getNewDoctor().getLastName(),
                        consentEntity.getNewDoctor().getHospitalSpecialization().getHospital().getHospitalName(),
                        consentEntity.getPatientConsent(),
                        consentEntity.getSeniorDoctorConsent(),
                        consentEntity.getLocalDate()
                )
        ).toList();

        List<ViewConsentResponse> viewApprovedConsentResponses = approvedConsentEntities.stream().map(
                consentEntity -> new ViewConsentResponse(
                        consentEntity.getId(),
                        consentEntity.getConnect().getPatient().getFirstName(),
                        consentEntity.getConnect().getPatient().getLastName(),
                        consentEntity.getConnect().getDoctor().getFirstName(),
                        consentEntity.getConnect().getDoctor().getLastName(),
                        consentEntity.getConnect().getDoctor().getHospitalSpecialization().getHeadDoctor().getFirstName(),
                        consentEntity.getConnect().getDoctor().getHospitalSpecialization().getHeadDoctor().getLastName(),
                        consentEntity.getNewDoctor().getFirstName(),
                        consentEntity.getNewDoctor().getLastName(),
                        consentEntity.getNewDoctor().getHospitalSpecialization().getHospital().getHospitalName(),
                        consentEntity.getPatientConsent(),
                        consentEntity.getSeniorDoctorConsent(),
                        consentEntity.getLocalDate()
                )
        ).toList();

        SeniorDoctorViewConsentResponse consentResponse = new SeniorDoctorViewConsentResponse();
        consentResponse.setApprovedConsents(viewApprovedConsentResponses);
        consentResponse.setPendingConsents(viewPendingConsentResponses);

        doctor.setLastAccessTime(doctorEntity.getEmail());

        return ResponseEntity.ok(consentResponse);
    }

    @PostMapping("/approveConsent/{id}")
    public ResponseEntity<?> approveConsent(@PathVariable Integer id, HttpServletRequest request ) {
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionSeniorDoctor(request);
        if (doctorEntity == null) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        ConsentEntity consentEntity = consent.getConsentById(id);
        if (consentEntity == null) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Please Select Valid Options");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        DoctorEntity newDoctor = consentEntity.getConnect().getDoctor().getHospitalSpecialization().getHeadDoctor();
        if (newDoctor != doctorEntity) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You are not authorized to access this");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        if(consentEntity.getSeniorDoctorConsent()=="rejected" || consentEntity.getPatientConsent()=="rejected"){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("This request was rejected before");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        consent.giveDoctorConsent(consentEntity.getId());
        doctor.setLastAccessTime(newDoctor.getEmail());

        consent.seniorDrApproved(consentEntity, consentEntity.getConnect().getPatient());
        consent.sendApprovalEmailToNewDoctor(consentEntity);
        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("The Consent to share the report has been granted");
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping("/rejectConsent/{id}")
    public ResponseEntity<?> rejectConsent(@PathVariable Integer id, HttpServletRequest request ) {
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionSeniorDoctor(request);
        if (doctorEntity == null) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        ConsentEntity consentEntity = consent.getConsentById(id);
        if (consentEntity == null) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Please Select Valid Options");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        DoctorEntity newDoctor = consentEntity.getConnect().getDoctor().getHospitalSpecialization().getHeadDoctor();
        if (newDoctor != doctorEntity) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You are not authorized to access this");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        if (Objects.equals(consentEntity.getSeniorDoctorConsent(), "accepted")) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have accepted it before you cannot reject now");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        } else if (Objects.equals(consentEntity.getSeniorDoctorConsent(), "rejected")) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("This request was rejected before you cannot reject again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        consent.rejectConsent(consentEntity.getId());
        doctor.setLastAccessTime(newDoctor.getEmail());

        consent.sendRejectionEmailToNewDoctor(consentEntity);
        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("The Consent to share the report has been rejected");
        return ResponseEntity.ok(successMessage);
    }
}
