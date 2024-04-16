package com.example.server.doctor;

import com.example.server.connection.ConnectionEntity;
import com.example.server.connection.ConnectionService;
import com.example.server.consent.ConsentEntity;
import com.example.server.consent.ConsentService;
import com.example.server.consultation.ConsultationService;
import com.example.server.dto.request.DoctorPatientConsent;
import com.example.server.dto.request.EmailRequest;
import com.example.server.dto.request.LoginUserRequest;
import com.example.server.dto.request.VerifyEmailRequest;
import com.example.server.dto.response.*;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.errorOrSuccessMessageResponse.ErrorMessage;
import com.example.server.hospitalSpecialization.HospitalSpecializationEntity;
import com.example.server.jwtToken.JWTService;
import com.example.server.jwtToken.JWTTokenReCheck;
import com.example.server.patient.PatientController;
import com.example.server.patient.PatientEntity;
import com.example.server.patient.PatientService;
import com.example.server.webSocket.DoctorStatusScheduler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/doctor")
@CrossOrigin
public class DoctorController {
    private final DoctorService doctor;
    private final JWTService jwtService;
    private final JWTTokenReCheck jwtTokenReCheck;
    private final PatientService patient;
    private final DoctorStatusScheduler doctorStatusScheduler;
    private final EmailSender emailSender;
    private final ConnectionService connection;
    private final ConsentService consent;
    private final ConsultationService consultation;

    public DoctorController(DoctorService doctor, JWTService jwtService, JWTTokenReCheck jwtTokenReCheck, PatientService patient, DoctorStatusScheduler doctorStatusScheduler, EmailSender emailSender, ConnectionService connection, ConsentService consent, ConsultationService consultation) {
        this.doctor = doctor;
        this.jwtService = jwtService;
        this.jwtTokenReCheck = jwtTokenReCheck;
        this.patient = patient;
        this.doctorStatusScheduler = doctorStatusScheduler;
        this.emailSender = emailSender;
        this.connection = connection;
        this.consent = consent;
        this.consultation = consultation;
    }

    @PostMapping("/login")
    ResponseEntity<?> loginDoctor(@RequestBody VerifyEmailRequest body) throws IOException {
        DoctorEntity newDoctor = doctor.verifyDoctor(
                body.getUser().getEmail(),
                body.getUser().getOtp()
        );
        if(newDoctor==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Wrong OTP or Wrong Email");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        List<ConnectionEntity> connectionEntities=connection.findAllConnectionsByDoctor(newDoctor);
        List<AppointmentDetailsDto> pastAppointmentDetails = consultation.findPastAppointments(connectionEntities);
        List<AppointmentDetailsDto> futureAppointmentDetails = consultation.findFutureAppointments(connectionEntities);
        Integer patientCount=connection.countPatient(newDoctor);
        Integer appointmentCount=consultation.countAppointments(connectionEntities);
        List<EachDayCount> eachDayCounts=consultation.sendEachDayCount(connectionEntities);
        DoctorLoginResponse doctorLoginResponse=new DoctorLoginResponse();
        doctorLoginResponse.setDoctorId(newDoctor.getId());
        doctorLoginResponse.setFirstName(newDoctor.getFirstName());
        doctorLoginResponse.setLastName(newDoctor.getLastName());
        doctorLoginResponse.setDegree(newDoctor.getDegree());
        doctorLoginResponse.setFirstTimeLogin(newDoctor.isFirstTimeLogin());
        doctorLoginResponse.setRegistrationId(newDoctor.getRegistrationId());
        doctorLoginResponse.setEachDayCounts(eachDayCounts);
        doctorLoginResponse.setTotalAppointments(appointmentCount);
        doctorLoginResponse.setPastAppointmentDetails(pastAppointmentDetails);
        doctorLoginResponse.setFutureAppointmentDetails(futureAppointmentDetails);
        doctorLoginResponse.setTotalPatients(patientCount);

        doctorStatusScheduler.sendDoctorStatusUpdate();

        String jwtToken = jwtService.createJwt(newDoctor.getEmail(), newDoctor.getRole());
        doctor.setJwtToken(jwtToken, newDoctor.getEmail());
        doctor.setLastAccessTime(newDoctor.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        return ResponseEntity.ok().headers(headers).body(doctorLoginResponse);
    }

    @PostMapping("/loginotp")
    ResponseEntity<?> loginDoctoremail(@RequestBody LoginUserRequest body){
        if(!doctor.checkDoctor(body.getUser().getEmail())){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Email ID is not Registered as a Doctor.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        DoctorEntity currentDoctor = doctor.doctorDetails(body.getUser().getEmail());
        if(currentDoctor==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Email ID is not Registered as a Doctor.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        String otp = emailSender.sendOtpEmail(
                body.getUser().getEmail(),
                currentDoctor.getFirstName()
        );
        DoctorEntity doctor1 = doctor.updateOtp(otp, currentDoctor.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    //JWTToken done
    @GetMapping("/departmentDetails")
    public ResponseEntity<?> getDepartmentOfDoctorId(HttpServletRequest request) {
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        if(doctorEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        HospitalSpecializationEntity hospitalSpecialization = doctorEntity.getHospitalSpecialization();
        //senior doctor name
        String headDoctorFirstName = hospitalSpecialization.getHeadDoctor().getFirstName();
        String headDoctorLastName = hospitalSpecialization.getHeadDoctor().getLastName();
        String headDoctorName = headDoctorFirstName + ' ' + headDoctorLastName;

        String currSpecialization = hospitalSpecialization.getSpecialization().getName();

        List<String> doctors = doctor.getDoctorsFromSameSpecialization(hospitalSpecialization);

        DepartmentDto departmentDto = new DepartmentDto(headDoctorName, currSpecialization, doctors);
        return ResponseEntity.ok(departmentDto);
    }

//TODO: send details of all patients of a doctor and all doctors to send consent
    @GetMapping("/viewPatientsAndDoctors")
    public ResponseEntity<?> getPatientsHospitalsDoctors(HttpServletRequest request){
        DoctorEntity mainDoctor = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        if(mainDoctor==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        PatientHospitalBranchDoctorResponse currData = new PatientHospitalBranchDoctorResponse();
        List<PatientEntity> patientEntities = connection.findAllPatientsByDoctor(mainDoctor);
        List<DoctorDetailsResponse> doctorStatusList = doctor.getAllDoctorDetails();

        List<NameResponse> patientDetails = patientEntities.stream().map(
                patientEntity -> new NameResponse(
                      patientEntity.getFirstName(),
                      patientEntity.getLastName(),
                      patientEntity.getEmail()
                )
        ).toList();
        PatientHospitalBranchDoctorResponse response = new PatientHospitalBranchDoctorResponse();
        response.setDoctorsList(doctorStatusList);
        response.setPatientList(patientDetails);

        return ResponseEntity.ok(response);
    }

    //JWT not needed
    @GetMapping("/landingPage")
    public ResponseEntity<List<DoctorDetailsResponse>> getDoctorDetails()
    {
        List<DoctorDetailsResponse> doctorDetailsResponses = doctor.getAllDoctorDetails();
        return ResponseEntity.ok(doctorDetailsResponses);
    }


    //JWT Done
    @PostMapping("/registerConsent")
    public ResponseEntity<?> registerConsent(@RequestBody DoctorPatientConsent doctorPatientConsent, HttpServletRequest request) {
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        if(doctorEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        PatientEntity patientEntity = patient.patientDetails(doctorPatientConsent.getPatientEmail());
        if(patientEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Could not find the requested patient under your inspection");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        ConnectionEntity connectionEntity = connection.findConnection(doctorEntity.getEmail(), patientEntity.getEmail());
        if(connectionEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Could not find the requested patient under your inspection");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        DoctorEntity newDoctorEntity = doctor.findDoctorByEmail(doctorPatientConsent.getNewDoctorEmail());
        if(newDoctorEntity==null || newDoctorEntity==doctorEntity){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Select a valid doctor");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setConnect(connectionEntity);
        consentEntity.setSeniorDoctorConsent(false);
        consentEntity.setPatientConsent(false);
        consentEntity.setNewDoctor(newDoctorEntity);
        consentEntity.setLocalDate(LocalDate.now());

        ConsentEntity consentEntity1 = consent.saveConsent(consentEntity);
        if(consentEntity1==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Could not save consent");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        emailSender.sendConsentEmailToPatient(patientEntity.getEmail(),patientEntity.getFirstName(),doctorEntity.getFirstName(),newDoctorEntity.getFirstName());
        emailSender.sendConsentEmailToSrDoctor(doctorEntity.getHospitalSpecialization().getHeadDoctor().getEmail(),doctorEntity.getHospitalSpecialization().getHeadDoctor().getFirstName(),patientEntity.getFirstName(),doctorEntity.getFirstName(),newDoctorEntity.getFirstName());
        return ResponseEntity.status(HttpStatus.CREATED).body("Registered for approval from Patient and Senior Doctor");
    }

}
