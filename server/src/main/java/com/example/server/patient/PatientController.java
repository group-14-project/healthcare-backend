package com.example.server.patient;

import com.example.server.aws.AwsServiceImplementation;
import com.example.server.aws.EncryptFile;
import com.example.server.aws.FileTypeEnum;
import com.example.server.common.CommonService;
import com.example.server.connection.ConnectionEntity;
import com.example.server.connection.ConnectionService;
import com.example.server.consent.ConsentEntity;
import com.example.server.consent.ConsentRepository;
import com.example.server.consent.ConsentService;
import com.example.server.consultation.ConsultationService;
import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorRepository;
import com.example.server.doctor.DoctorService;
import com.example.server.dto.request.*;
import com.example.server.dto.response.*;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.errorOrSuccessMessageResponse.ErrorMessage;
import com.example.server.errorOrSuccessMessageResponse.SuccessMessage;
import com.example.server.jwtToken.JWTService;
import com.example.server.jwtToken.JWTTokenReCheck;
import com.example.server.report.ReportEntity;
import com.example.server.report.ReportService;
import com.example.server.reviews.ReviewEntity;
import com.example.server.reviews.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/patient")
@CrossOrigin
public class   PatientController {
    private final PatientService patient;
    private final EncryptFile encryptFile;
    private final EmailSender emailSender;
    private final AwsServiceImplementation awsServiceImplementation;
    private final JWTService jwtService;
    private final ReportService report;
    private final JWTTokenReCheck jwtTokenReCheck;
    private final ConsentService consent;
    private final DoctorRepository doctorRepository;
    private final ConnectionService connection;
    private final ConsultationService consultation;
    private final ReviewService reviewService;

    private final ConsentRepository consentRepository;

    private final ConnectionService connectionService;

    private final CommonService commonService;



    public PatientController(PatientService patient, EncryptFile encryptFile, EmailSender emailSender, AwsServiceImplementation awsServiceImplementation, JWTService jwtService, ReportService report, JWTTokenReCheck jwtTokenReCheck, ConsentService consent, DoctorRepository doctorRepository, ConnectionService connection, ConsultationService consultation, ReviewService reviewService, CommonService commonService, DoctorService doctorService, ConsentRepository consentRepository, ConnectionService connectionService){
        this.patient = patient;
        this.encryptFile = encryptFile;
        this.emailSender = emailSender;
        this.awsServiceImplementation = awsServiceImplementation;
        this.jwtService = jwtService;
        this.report = report;
        this.jwtTokenReCheck = jwtTokenReCheck;
        this.consent = consent;
        this.doctorRepository = doctorRepository;
        this.connection = connection;
        this.consultation = consultation;
        this.reviewService = reviewService;
        this.commonService = commonService;
        this.consentRepository = consentRepository;
        this.connectionService = connectionService;
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

        if(newPatient==null)
        {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You dont have an account, please sign up");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        newPatient.setDeleteEntry(false);
        List<ConnectionEntity> connectionEntities = connection.findAllConnections(newPatient);
        List<DoctorDetailsResponse> doctorDetailsResponses = new ArrayList<>();
        for(ConnectionEntity connectionEntity: connectionEntities){
            DoctorDetailsResponse doctorDetailsResponse = new DoctorDetailsResponse();
            doctorDetailsResponse.setDoctorEmail(connectionEntity.getDoctor().getEmail());
            doctorDetailsResponse.setDegree(connectionEntity.getDoctor().getDegree());
            doctorDetailsResponse.setFirstName(connectionEntity.getDoctor().getFirstName());
            doctorDetailsResponse.setLastName(connectionEntity.getDoctor().getLastName());
            doctorDetailsResponse.setSpecialization(connectionEntity.getDoctor().getHospitalSpecialization().getSpecialization().getName());
            doctorDetailsResponse.setHospitalName(connectionEntity.getDoctor().getHospitalSpecialization().getHospital().getHospitalName());
            doctorDetailsResponse.setImageUrl(connectionEntity.getDoctor().getImageUrl());
            doctorDetailsResponses.add(doctorDetailsResponse);
        }
        List<AppointmentDetailsDto> pastAppointmentDetails = consultation.findPastAppointments(connectionEntities);
        List<AppointmentDetailsDto> futureAppointmentDetails = consultation.findFutureAppointments(connectionEntities);

        PatientResponse patientResponse = new PatientResponse(newPatient.getId(),
                newPatient.getEmail(), newPatient.getFirstName(), newPatient.getLastName(), commonService.decrypt(newPatient.getHeight()),commonService.decrypt(newPatient.getWeight()),commonService.decrypt(newPatient.getBloodGroup()),
                newPatient.getGender(), newPatient.isFirstTimeLogin(), pastAppointmentDetails, futureAppointmentDetails, doctorDetailsResponses);

        String jwtToken = jwtService.createJwt(newPatient.getEmail(), newPatient.getRole());
        patient.setJwtToken(jwtToken, newPatient.getEmail());
        patient.setLastAccessTime(newPatient.getEmail());

        ResponseCookie cookie = ResponseCookie.from("jwtToken", jwtToken)
                .httpOnly(true)
                .path("/") // Set the cookie path as per your requirements
                .maxAge(36000) // Set the cookie expiration time in seconds
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

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
        LocalDate deletionTime = currentPatient.getDeletionTime();
        if(deletionTime!=null)
        {
            LocalDate currentDate = LocalDate.now();
            long daysBetween = ChronoUnit.DAYS.between(deletionTime, currentDate);
            if (daysBetween > (365 * 2))
            {
                ErrorMessage errorMessage = new ErrorMessage();
                errorMessage.setErrorMessage("Deleted Account try login with new email");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            }
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
        if(patient.checkPatientVerification(body.getPatient().getEmail())) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Email Already Exist");
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
                newPatient.getEmail(), newPatient.getFirstName(), newPatient.getLastName(), commonService.decrypt(newPatient.getHeight()), commonService.decrypt(newPatient.getWeight()),commonService.decrypt(newPatient.getBloodGroup()),
                newPatient.getGender(), newPatient.isFirstTimeLogin(), pastAppointmentDetails, futureAppointmentDetails);

        patient.setLastAccessTime(patientEntity.getEmail());
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

        patient.setLastAccessTime(patientEntity.getEmail());

        return ResponseEntity.ok(viewConsentResponses);
    }

    @PutMapping("/giveConsent")
    public ResponseEntity<?> giveConsent(@RequestBody GiveConsentRequest giveConsentRequest, HttpServletRequest request ){
        PatientEntity patientEntity = jwtTokenReCheck.checkJWTAndSessionPatient(request);
        if(patientEntity==null)
        {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        ConsentEntity consentEntity = consent.getConsentById(giveConsentRequest.getConsentId());
        if(consentEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Please Select Valid Options");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        PatientEntity newPatient = consentEntity.getConnect().getPatient();
        if(newPatient!=patientEntity){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You are not authorized to access this");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        if(Objects.equals(consentEntity.getPatientConsent(), "rejected")){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have already rejected the consent");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        consent.givePatientConsent(consentEntity.getId());

        patient.setLastAccessTime(patientEntity.getEmail());

        consent.patientApproved(consentEntity, patientEntity);

        consent.sendApprovalEmailToNewDoctor(consentEntity);

        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("The Consent to share the report has been approved");
        return ResponseEntity.ok(successMessage);
    }

    @PutMapping("/withdrawConsent")
    public ResponseEntity<?> withdrawConsent(@RequestBody GiveConsentRequest giveConsentRequest, HttpServletRequest request){
        PatientEntity patientEntity = jwtTokenReCheck.checkJWTAndSessionPatient(request);
        if(patientEntity==null)
        {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        ConsentEntity consentEntity = consent.getConsentById(giveConsentRequest.getConsentId());
        if(consentEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Please Select Valid Options");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        PatientEntity newPatient = consentEntity.getConnect().getPatient();
        if(newPatient!=patientEntity){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You are not authorized to access this");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        if(Objects.equals(consentEntity.getPatientConsent(), "rejected")){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have already rejected the consent");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        if(Objects.equals(consentEntity.getPatientConsent(), "pending")){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have to Accept or Reject. You cannot withdraw without accepting first");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        consent.withdrawPatientConsent(consentEntity.getId());

        patient.setLastAccessTime(patientEntity.getEmail());

        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("The Consent to share the report has been withdrawn");
        return ResponseEntity.ok(successMessage);
    }

    @PutMapping("/rejectConsent")
    public ResponseEntity<?> rejectConsent(@RequestBody GiveConsentRequest giveConsentRequest, HttpServletRequest request) {
        PatientEntity patientEntity = jwtTokenReCheck.checkJWTAndSessionPatient(request);
        if (patientEntity == null) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        ConsentEntity consentEntity = consent.getConsentById(giveConsentRequest.getConsentId());
        if (consentEntity == null) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Please Select Valid Options");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        PatientEntity newPatient = consentEntity.getConnect().getPatient();
        if (newPatient != patientEntity) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You are not authorized to access this");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        if (Objects.equals(consentEntity.getPatientConsent(), "rejected")) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have already rejected the consent");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        else if (Objects.equals(consentEntity.getPatientConsent(), "withdrawn")) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You cannot reject, you have withdrawn first");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        else if (Objects.equals(consentEntity.getPatientConsent(), "accepted")) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You cannot reject, you have accepted first");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        consent.rejectConsent(consentEntity.getId());
        consent.sendRejectionEmailToNewDoctor(consentEntity);
        patient.setLastAccessTime(patientEntity.getEmail());

        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("The Consent to share the report has been withdrawn");
        return ResponseEntity.ok(successMessage);
    }


    @PostMapping(value = "/uploadReport", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadFile(@ModelAttribute ReportUploadRequest reportUploadRequest, HttpServletRequest request) throws Exception {
        PatientEntity patientEntity = jwtTokenReCheck.checkJWTAndSessionPatient(request);
        if(patientEntity==null)
        {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        if (reportUploadRequest.getFile().isEmpty()) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("No PDF uploaded");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        if (!"application/pdf".equals(reportUploadRequest.getFile().getContentType())) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("File is not a PDF");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);

        }
        String fileName = StringUtils.cleanPath(reportUploadRequest.getFile().getOriginalFilename());
        String contentType = reportUploadRequest.getFile().getContentType();
        InputStream inputStream = reportUploadRequest.getFile().getInputStream();
//        long filesize = reportUploadRequest.getFile().getSize();
        byte[] encryptedBytes = encryptFile.encryptFile(inputStream);

        // Create an InputStream from the encrypted content
        InputStream encryptedInputStream = new ByteArrayInputStream(encryptedBytes);
        if(!awsServiceImplementation.uploadFile("adityavit36",fileName, (long)encryptedBytes.length, contentType, encryptedInputStream)){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Unexpected Error Please upload Again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        //TODO: work to be done
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(reportUploadRequest.getDoctorEmail());

        ConnectionEntity connectionEntity = connection.findConnection(reportUploadRequest.getDoctorEmail(), patientEntity.getEmail());

        if(doctor!=null && connectionEntity == null) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Doctor not Found, please try again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        ReportEntity reportEntity = report.addReport(fileName, patientEntity, connectionEntity, reportUploadRequest.getReportName());
        if(reportEntity == null) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Unexpected Error Please upload Again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        patient.setLastAccessTime(patientEntity.getEmail());

        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("File has been uploaded Successfully");
        return ResponseEntity.ok(successMessage);
    }

    @GetMapping("/downloadFile/{id}")
    public ResponseEntity<?> downloadFile(@PathVariable("id") Integer id, HttpServletRequest request) throws Exception {
        PatientEntity patientEntity = jwtTokenReCheck.checkJWTAndSessionPatient(request);
        if(patientEntity==null)
        {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        ReportEntity reportEntity = report.findReportById(id);
        if(!Objects.equals(reportEntity.getPat().getEmail(), patientEntity.getEmail())){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You are not allowed to access this report");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        String bucketName = "adityavit36";
        val encryptedBody = awsServiceImplementation.downloadFile(bucketName,reportEntity.getFileName()); // Decrypt the file content
        byte[] decryptedBytes = encryptFile.decryptFile(encryptedBody.toByteArray());
        patient.setLastAccessTime(patientEntity.getEmail());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", reportEntity.getFileName());
        headers.setContentType(FileTypeEnum.fromFilename(reportEntity.getFileName()));

        patient.setLastAccessTime(patientEntity.getEmail());
        return ResponseEntity.ok()
                .headers(headers)
                .body(decryptedBytes);
    }

    @PostMapping("/addReview")
    public ResponseEntity<?> addReview(@RequestBody AddReviewRequest reviewRequest, HttpServletRequest request){
        PatientEntity patientEntity = jwtTokenReCheck.checkJWTAndSessionPatient(request);
        if(patientEntity==null)
        {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        ConnectionEntity connectionEntity = connection.findConnection(reviewRequest.getDoctorEmail(), patientEntity.getEmail());
        if(connectionEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have never consulted this doctor so you cant review them");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        ReviewEntity review = reviewService.addReview(connectionEntity, reviewRequest.getReview());
        if(review==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("There was an error registering the review please try again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        patient.setLastAccessTime(patientEntity.getEmail());
        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("Review has been uploaded Successfully");
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping("/viewReviews")
    public ResponseEntity<?> viewReviewsOfaDoctor(@RequestBody EmailRequest emailRequest, HttpServletRequest request){
        PatientEntity patientEntity = jwtTokenReCheck.checkJWTAndSessionPatient(request);
        if(patientEntity==null)
        {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        DoctorEntity doctorEntity = doctorRepository.findDoctorEntitiesByEmail(emailRequest.getEmail());
        List<ConnectionEntity> connectionEntities = connection.findAllConnectionsByDoctor(doctorEntity);
        List<ViewReviewsResponse> viewReviewsResponses = reviewService.viewReviewsByConnection(connectionEntities);
        patient.setLastAccessTime(doctorEntity.getEmail());
        return ResponseEntity.ok(viewReviewsResponses);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) throws IOException {
        PatientEntity patientEntity = jwtTokenReCheck.checkJWTAndSessionPatient(request);
        if (patientEntity == null) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have been logged out");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        patient.expireJWTfromTable(patientEntity.getEmail());
        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("You have been logged out");
        return ResponseEntity.ok(successMessage);
    }

    @GetMapping("/viewReports")
    public ResponseEntity<?> viewReports(HttpServletRequest request){
        PatientEntity patientEntity = jwtTokenReCheck.checkJWTAndSessionPatient(request);
        if (patientEntity == null) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have been logged out");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        List<ConnectionEntity> connectionEntities = connection.findAllConnections(patientEntity);
        List<ReportDetailsResponse> reportDetailsResponses = report.findAllReportsByConnectionListAndBlank(connectionEntities, patientEntity);
        return ResponseEntity.ok(reportDetailsResponses);
    }

    @DeleteMapping("/deletePatient")
    public ResponseEntity<?> deletePatient(HttpServletRequest request)
    {
        PatientEntity patientEntity = jwtTokenReCheck.checkJWTAndSessionPatient(request);
        if (patientEntity == null)
        {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have been logged out");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        List<ConnectionEntity> connectionEntities=connectionService.findPatientConnection(patientEntity);
        if(connectionEntities!=null) {
            for (ConnectionEntity connectionEntity : connectionEntities) {
                ConsentEntity consentEntity = consentRepository.findConsentById(connectionEntity.getId());
                if (consentEntity != null) {
                    consent.withdrawPatientConsent(consentEntity.getId());
                }
            }
        }
        patient.deleteAccount(patientEntity.getEmail());
        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("Account has been deleted");
        return ResponseEntity.ok(successMessage);
    }
}
