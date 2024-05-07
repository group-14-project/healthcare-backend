package com.example.server.doctor;

import com.example.server.aws.AwsServiceImplementation;
import com.example.server.aws.EncryptFile;
import com.example.server.aws.FileTypeEnum;
import com.example.server.connection.ConnectionEntity;
import com.example.server.connection.ConnectionService;
import com.example.server.consent.ConsentEntity;
import com.example.server.consent.ConsentService;
import com.example.server.consultation.ConsultationEntity;
import com.example.server.consultation.ConsultationService;
import com.example.server.dto.request.*;
import com.example.server.dto.response.*;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.errorOrSuccessMessageResponse.ErrorMessage;
import com.example.server.errorOrSuccessMessageResponse.SuccessMessage;
import com.example.server.hospitalSpecialization.HospitalSpecializationEntity;
import com.example.server.jwtToken.JWTService;
import com.example.server.jwtToken.JWTTokenReCheck;
import com.example.server.patient.PatientController;
import com.example.server.patient.PatientEntity;
import com.example.server.patient.PatientService;
import com.example.server.report.ReportEntity;
import com.example.server.report.ReportService;
import com.example.server.reviews.ReviewService;
import com.example.server.webSocket.DoctorStatusScheduler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private final ReviewService review;
    private final ReportService report;
    private final AwsServiceImplementation awsServiceImplementation;
    private final EncryptFile encryptFile;

    public DoctorController(DoctorService doctor, JWTService jwtService, JWTTokenReCheck jwtTokenReCheck, PatientService patient, DoctorStatusScheduler doctorStatusScheduler, EmailSender emailSender, ConnectionService connection, ConsentService consent, ConsultationService consultation, ReviewService reviewService, ReportService report, AwsServiceImplementation awsServiceImplementation, EncryptFile encryptFile) {
        this.doctor = doctor;
        this.jwtService = jwtService;
        this.jwtTokenReCheck = jwtTokenReCheck;
        this.patient = patient;
        this.doctorStatusScheduler = doctorStatusScheduler;
        this.emailSender = emailSender;
        this.connection = connection;
        this.consent = consent;
        this.consultation = consultation;
        this.review = reviewService;
        this.report = report;
        this.awsServiceImplementation = awsServiceImplementation;
        this.encryptFile = encryptFile;
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

        doctor.loginStatusChange(newDoctor);

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

        ResponseCookie cookie = ResponseCookie.from("jwtToken", jwtToken)
                .httpOnly(true)
                .path("/") // Set the cookie path as per your requirements
                .maxAge(36000) // Set the cookie expiration time in seconds
                .build();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

        headers.setBearerAuth(jwtToken);

        return ResponseEntity.ok().headers(headers).body(doctorLoginResponse);
    }

    @PostMapping("/loginotp")
    ResponseEntity<?> loginDoctoremail(@RequestBody LoginUserRequest body) throws IOException {
        if(!doctor.checkDoctor(body.getUser().getEmail())){
            doctorStatusScheduler.sendDoctorStatusUpdate();
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
        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("OTP Sent to Email Successfully");
        return ResponseEntity.ok(successMessage);
    }


    //JWTToken done
    @GetMapping("/departmentDetails")
    public ResponseEntity<?> getDepartmentOfDoctorId(HttpServletRequest request) throws IOException {
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        if(doctorEntity==null){
            doctorStatusScheduler.sendDoctorStatusUpdate();
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        HospitalSpecializationEntity hospitalSpecialization = doctorEntity.getHospitalSpecialization();

        DoctorDetailsResponse seniorDoctor = new DoctorDetailsResponse();
        seniorDoctor.setImageUrl(hospitalSpecialization.getHeadDoctor().getImageUrl());
        seniorDoctor.setDoctorEmail(hospitalSpecialization.getHeadDoctor().getEmail());
        seniorDoctor.setDegree(hospitalSpecialization.getHeadDoctor().getDegree());
        seniorDoctor.setSpecialization(hospitalSpecialization.getSpecialization().getName());
        seniorDoctor.setFirstName(hospitalSpecialization.getHeadDoctor().getFirstName());
        seniorDoctor.setLastName(hospitalSpecialization.getHeadDoctor().getLastName());
        seniorDoctor.setHospitalName(hospitalSpecialization.getHospital().getHospitalName());

        String currSpecialization = hospitalSpecialization.getSpecialization().getName();

        List<DoctorDetailsResponse> doctors = doctor.getDoctorsInSpecialization(hospitalSpecialization);

        DepartmentDto departmentDto = new DepartmentDto(seniorDoctor, currSpecialization, doctors);
        doctor.setLastAccessTime(doctorEntity.getEmail());

        return ResponseEntity.ok(departmentDto);
    }


    @GetMapping("/viewHospitalsAndDoctors")
    public ResponseEntity<?> getHospitalsDoctors(HttpServletRequest request) throws IOException {
        System.out.println("Errror 1");
        DoctorEntity mainDoctor = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        System.out.println("Errror 2");
        if(mainDoctor==null){
            doctorStatusScheduler.sendDoctorStatusUpdate();
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        List<HospitalBranchDoctorResponse> currData = doctor.viewAllHospitalsAndDoctors();

        doctor.setLastAccessTime(mainDoctor.getEmail());
        return ResponseEntity.ok(currData);
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
    public ResponseEntity<?> registerConsent(@RequestBody DoctorPatientConsent doctorPatientConsent, HttpServletRequest request) throws IOException {
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        if(doctorEntity==null){
            doctorStatusScheduler.sendDoctorStatusUpdate();
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
        ConsentEntity consentEntity1 = consent.getConsentByConnectionAndSeniorDoctor(connectionEntity, newDoctorEntity);

        if(consentEntity1==null) {
            ConsentEntity consentEntity = new ConsentEntity();
            consentEntity.setConnect(connectionEntity);
            consentEntity.setSeniorDoctorConsent("pending");
            consentEntity.setPatientConsent("pending");
            consentEntity.setNewDoctor(newDoctorEntity);
            consentEntity.setLocalDate(LocalDate.now());
            consent.saveNewConsent(consentEntity);
        }else{
            if(Objects.equals(consentEntity1.getPatientConsent(), "rejected") || Objects.equals(consentEntity1.getSeniorDoctorConsent(), "rejected")) {
                consentEntity1.setPatientConsent("pending");
                consentEntity1.setSeniorDoctorConsent("pending");
                consent.saveNewConsent(consentEntity1);
            }else{
                ErrorMessage errorMessage = new ErrorMessage();
                errorMessage.setErrorMessage("The consent request already exist");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            }
        }
        emailSender.sendConsentEmailToPatient(patientEntity.getEmail(),patientEntity.getFirstName(),doctorEntity.getFirstName(),newDoctorEntity.getFirstName());
        emailSender.sendConsentEmailToSrDoctor(doctorEntity.getHospitalSpecialization().getHeadDoctor().getEmail(),doctorEntity.getHospitalSpecialization().getHeadDoctor().getFirstName(),patientEntity.getFirstName(),doctorEntity.getFirstName(),newDoctorEntity.getFirstName());
        return ResponseEntity.status(HttpStatus.CREATED).body("Registered for approval from Patient and Senior Doctor");
    }

    @PutMapping("/updatePassword")
    ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateRequest body, HttpServletRequest request) throws IOException {
        System.out.println("Errror 1");
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        System.out.println("Errror 2");
        if(doctorEntity==null){
            doctorStatusScheduler.sendDoctorStatusUpdate();
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        System.out.println("Errror 3");
        DoctorEntity newDoctor = doctor.updatePassword(doctorEntity.getEmail(), body.getPassword());

        System.out.println("Errror 4");
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
        doctor.setLastAccessTime(newDoctor.getEmail());

        return ResponseEntity.ok().body(doctorLoginResponse);
    }

    @GetMapping("/patientsLastAppointment")
    public ResponseEntity<?> getPatientNameAndLastDetails(HttpServletRequest request) throws IOException {
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        if(doctorEntity==null){
            doctorStatusScheduler.sendDoctorStatusUpdate();
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        List<ConnectionEntity> connectionEntities = connection.findAllConnectionsByDoctor(doctorEntity);
        List<ConsultationEntity> consultationEntities = consultation.findLatestAppointment(connectionEntities);

        List<PatientAndLastAppointmentTime> response = new ArrayList<>();

        for (ConsultationEntity consultationEntity : consultationEntities) {
            PatientAndLastAppointmentTime patientAndLastAppointmentTime = new PatientAndLastAppointmentTime();
            patientAndLastAppointmentTime.setFirstName(consultationEntity.getConnectionId().getPatient().getFirstName());
            patientAndLastAppointmentTime.setLastName(consultationEntity.getConnectionId().getPatient().getLastName());
            patientAndLastAppointmentTime.setDate(consultationEntity.getAppointmentDateAndTime());
            patientAndLastAppointmentTime.setEmail(consultationEntity.getConnectionId().getPatient().getEmail());
            response.add(patientAndLastAppointmentTime);
        }

        doctor.setLastAccessTime(doctorEntity.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/viewReviews")
    public ResponseEntity<?> viewReviews(HttpServletRequest request) throws IOException {
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        if(doctorEntity==null){

            doctorStatusScheduler.sendDoctorStatusUpdate();
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        List<ConnectionEntity> connectionEntities = connection.findAllConnectionsByDoctor(doctorEntity);
        List<ViewReviewsResponse> viewReviewsResponses = review.viewReviewsByConnection(connectionEntities);
        doctor.setLastAccessTime(doctorEntity.getEmail());
        return ResponseEntity.ok(viewReviewsResponses);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) throws IOException {
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        if (doctorEntity == null) {
            doctorStatusScheduler.sendDoctorStatusUpdate();
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have been logged out");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        doctor.makeDoctorOffline(doctorEntity.getEmail());
        doctor.expireJWTfromTable(doctorEntity.getEmail());
        doctorStatusScheduler.sendDoctorStatusUpdate();
        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("You have been logged out");
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping("/viewReports")
    public ResponseEntity<?> viewReports(@RequestBody EmailRequest emailRequest, HttpServletRequest request) throws IOException {
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        if (doctorEntity == null) {
            doctorStatusScheduler.sendDoctorStatusUpdate();
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have been logged out");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        PatientEntity patientEntity = patient.patientDetails(emailRequest.getEmail());
        if(patientEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("No such patient found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        ConnectionEntity connectionEntity = connection.findConnection(doctorEntity.getEmail(), emailRequest.getEmail());
        List<ReportDetailsResponse> reportDetailsResponses = report.findAllReportsByConnection(connectionEntity);
        doctor.setLastAccessTime(doctorEntity.getEmail());
        return ResponseEntity.ok(reportDetailsResponses);
    }

    @GetMapping("/downloadFile/{id}")
    public ResponseEntity<?> downloadFile(@PathVariable("id") Integer id, HttpServletRequest request) throws Exception {
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        if (doctorEntity == null) {
            doctorStatusScheduler.sendDoctorStatusUpdate();
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have been logged out");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        ReportEntity reportEntity = report.findReportById(id);
        if(reportEntity==null || (reportEntity.getCon()!=null && !Objects.equals(reportEntity.getCon().getDoctor().getEmail(), doctorEntity.getEmail()))){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You are not allowed to access this report");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        String bucketName = "adityavit36";
        val encryptedBody = awsServiceImplementation.downloadFile(bucketName,reportEntity.getFileName()); // Decrypt the file content
        byte[] decryptedBytes = encryptFile.decryptFile(encryptedBody.toByteArray());
//        patient.setLastAccessTime(doctorEntity.getEmail());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", reportEntity.getFileName());
        headers.setContentType(FileTypeEnum.fromFilename(reportEntity.getFileName()));
        doctor.setLastAccessTime(doctorEntity.getEmail());
//        patient.setLastAccessTime(doctorEntity.getEmail());
        return ResponseEntity.ok()
                .headers(headers)
                .body(decryptedBytes);
    }

    @GetMapping("/changeStatus/{id}")
    public ResponseEntity<?> changeStatus(@PathVariable Integer id,  HttpServletRequest request) throws IOException {
        System.out.println("hello from change status");
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        if (doctorEntity == null) {
            doctorStatusScheduler.sendDoctorStatusUpdate();
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have been logged out");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        doctor.changeStatus(doctorEntity, id);
        doctorStatusScheduler.sendDoctorStatusUpdate();
        doctor.setLastAccessTime(doctorEntity.getEmail());
        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("Status is changed");
        return ResponseEntity.ok(successMessage);
    }

    @GetMapping("/getRecommendedPatients")
    public ResponseEntity<?> getRecommendedPatients(HttpServletRequest request) throws IOException {
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        if (doctorEntity == null) {
            doctorStatusScheduler.sendDoctorStatusUpdate();
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have been logged out");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        List<ConsentEntity> consentEntities = consent.findApprovedConsentByDoctorAndPatientByNewDoctor(doctorEntity);
        if(consentEntities==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You dont have any recommended patients");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        List<RecommendedPatients> recommendedPatients = new ArrayList<>();
        for(ConsentEntity consentEntity: consentEntities){
            RecommendedPatients recommendedPatients1 = new RecommendedPatients();
            recommendedPatients1.setDoctorFirstName(consentEntity.getConnect().getDoctor().getFirstName());
            recommendedPatients1.setDoctorLastName(consentEntity.getConnect().getDoctor().getLastName());
            recommendedPatients1.setPatientFirstName(consentEntity.getConnect().getPatient().getFirstName());
            recommendedPatients1.setPatientLastName(consentEntity.getConnect().getPatient().getLastName());
            recommendedPatients1.setLocalDate(consentEntity.getLocalDate());
            recommendedPatients1.setConsentId(consentEntity.getId());
            recommendedPatients.add(recommendedPatients1);
        }
        doctor.setLastAccessTime(doctorEntity.getEmail());
        return ResponseEntity.ok(recommendedPatients);
    }

    @GetMapping("/viewRecommendedReports/{id}")
    public ResponseEntity<?> viewRecommendedReports(@PathVariable Integer id, HttpServletRequest request) throws IOException {
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        if (doctorEntity == null) {
            doctorStatusScheduler.sendDoctorStatusUpdate();
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have been logged out");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        ConnectionEntity connectionEntity = consent.findByNewDoctorAndID(doctorEntity, id);
        if(connectionEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You dont have access to this patient");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        List<ReportDetailsResponse> reportDetailsResponses = report.findAllReportsByConnection(connectionEntity);
        doctor.setLastAccessTime(doctorEntity.getEmail());
        return ResponseEntity.ok(reportDetailsResponses);
    }

    @GetMapping("/downloadRecommendedFile/{id}")
    public ResponseEntity<?> downloadRecommendedFile(@PathVariable("id") Integer id, HttpServletRequest request) throws Exception {
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        if (doctorEntity == null) {
            doctorStatusScheduler.sendDoctorStatusUpdate();
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have been logged out");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        ReportEntity reportEntity = report.findReportById(id);
        if(reportEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You are not allowed to access this report");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        ConnectionEntity connectionEntity = reportEntity.getCon();
        ConsentEntity consentEntity = consent.getConsentByConnectionAndSeniorDoctor(connectionEntity, doctorEntity);
        if(consentEntity==null || !Objects.equals(consentEntity.getSeniorDoctorConsent(), "accepted") || !Objects.equals(consentEntity.getPatientConsent(), "accepted")){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You are not allowed to access this report");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        String bucketName = "adityavit36";
        val encryptedBody = awsServiceImplementation.downloadFile(bucketName,reportEntity.getFileName()); // Decrypt the file content
        byte[] decryptedBytes = encryptFile.decryptFile(encryptedBody.toByteArray());
//        patient.setLastAccessTime(doctorEntity.getEmail());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", reportEntity.getFileName());
        headers.setContentType(FileTypeEnum.fromFilename(reportEntity.getFileName()));

        doctor.setLastAccessTime(doctorEntity.getEmail());
//        patient.setLastAccessTime(doctorEntity.getEmail());
        return ResponseEntity.ok()
                .headers(headers)
                .body(decryptedBytes);
    }

    @PostMapping("/addPrescription")
    public ResponseEntity<?> addPrescriptionRecordLink(@RequestBody AddPrescriptionRecordingLink body, HttpServletRequest request) throws IOException {
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        if (doctorEntity == null) {
            doctorStatusScheduler.sendDoctorStatusUpdate();
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have been logged out");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        PatientEntity patientEntity = patient.patientDetails(body.getPatientEmail());
        if(patientEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Could not find the patient");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        ConnectionEntity connectionEntity = connection.findConnection(doctorEntity.getEmail(), patientEntity.getEmail());
        if(connectionEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("This is not your patient");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        ConsultationEntity consultationEntity = consultation.setPrescription(body.getPrescription(), connectionEntity);
        if(consultationEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("This is not your patient");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        doctor.setLastAccessTime(doctorEntity.getEmail());
        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("Prescription is added");
        return ResponseEntity.ok(successMessage);
    }
}
