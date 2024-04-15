package com.example.server.doctor;

import com.example.server.connection.ConnectionEntity;
import com.example.server.connection.ConnectionService;
import com.example.server.consent.ConsentEntity;
import com.example.server.consent.ConsentService;
import com.example.server.dto.response.SeniorDoctorViewConsentResponse;
import com.example.server.dto.response.ViewConsentResponse;
import com.example.server.errorOrSuccessMessageResponse.ErrorMessage;
import com.example.server.jwtToken.JWTService;
import com.example.server.jwtToken.JWTTokenReCheck;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/senior_doctor")
@CrossOrigin
public class SeniorDoctorController {

    private final DoctorService doctor;
    private final JWTTokenReCheck jwtTokenReCheck;
    private final JWTService jwtService;
    private final ConnectionService connection;
    private final ConsentService consent;

    public SeniorDoctorController(DoctorService doctor, JWTTokenReCheck jwtTokenReCheck, JWTService jwtService, ConnectionService connection, ConsentService consent) {
        this.doctor = doctor;
        this.jwtTokenReCheck = jwtTokenReCheck;
        this.jwtService = jwtService;
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
                        consentEntity.getConnect().getDoctor().getHospitalSpecializationhead().getHeadDoctor().getFirstName(),
                        consentEntity.getConnect().getDoctor().getHospitalSpecializationhead().getHeadDoctor().getLastName(),
                        consentEntity.getNewDoctor().getFirstName(),
                        consentEntity.getNewDoctor().getLastName(),
                        consentEntity.isPatientConsent(),
                        consentEntity.isSeniorDoctorConsent(),
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
                        consentEntity.getConnect().getDoctor().getHospitalSpecializationhead().getHeadDoctor().getFirstName(),
                        consentEntity.getConnect().getDoctor().getHospitalSpecializationhead().getHeadDoctor().getLastName(),
                        consentEntity.getNewDoctor().getFirstName(),
                        consentEntity.getNewDoctor().getLastName(),
                        consentEntity.isPatientConsent(),
                        consentEntity.isSeniorDoctorConsent(),
                        consentEntity.getLocalDate()
                )
        ).toList();

        SeniorDoctorViewConsentResponse consentResponse = new SeniorDoctorViewConsentResponse();
        consentResponse.setApprovedConsents(viewApprovedConsentResponses);
        consentResponse.setPendingConsents(viewPendingConsentResponses);

        doctor.setLastAccessTime(doctorEntity.getEmail());

        return ResponseEntity.ok(consentResponse);
    }


}
