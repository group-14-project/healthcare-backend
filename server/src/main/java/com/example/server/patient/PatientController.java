package com.example.server.patient;

import com.example.server.aws.AwsServiceImplementation;
import com.example.server.aws.EncryptFile;
import com.example.server.aws.FileTypeEnum;
import com.example.server.connection.ConnectionEntity;
import com.example.server.connection.ConnectionService;
import com.example.server.consent.ConsentEntity;
import com.example.server.consent.ConsentService;
import com.example.server.consultation.ConsultationService;
import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorRepository;
import com.example.server.dto.request.*;
import com.example.server.dto.response.*;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.errorOrSuccessMessageResponse.ErrorMessage;
import com.example.server.errorOrSuccessMessageResponse.SuccessMessage;
import com.example.server.jwtToken.JWTService;
import com.example.server.jwtToken.JWTTokenReCheck;
import com.example.server.report.ReportEntity;
import com.example.server.report.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public PatientController(PatientService patient, EncryptFile encryptFile, EmailSender emailSender, AwsServiceImplementation awsServiceImplementation, JWTService jwtService, ReportService report, JWTTokenReCheck jwtTokenReCheck, ConsentService consent, DoctorRepository doctorRepository, ConnectionService connection, ConsultationService consultation){
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
                        consentEntity.getConnect().getDoctor().getHospitalSpecializationhead().getHeadDoctor().getFirstName(),
                        consentEntity.getConnect().getDoctor().getHospitalSpecializationhead().getHeadDoctor().getLastName(),
                        consentEntity.getNewDoctor().getFirstName(),
                        consentEntity.getNewDoctor().getLastName(),
                        consentEntity.getNewDoctor().getHospitalSpecialization().getHospital().getHospitalName(),
                        consentEntity.isPatientConsent(),
                        consentEntity.isSeniorDoctorConsent(),
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
        consent.givePatientConsent(consentEntity.getId());

        patient.setLastAccessTime(patientEntity.getEmail());

        consent.patientApproved(consentEntity, patientEntity);

        consent.sendApprovalEmailToNewDoctor(consentEntity);


        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("The Consent to share the report has been granted");
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
        consent.withdrawPatientConsent(consentEntity.getId());

        patient.setLastAccessTime(patientEntity.getEmail());

        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("The Consent to share the report has been withdrawn");
        return ResponseEntity.ok(successMessage);
    }


    @PostMapping("/uploadReport/{id}")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @PathVariable("id") Integer id, HttpServletRequest request) throws Exception {
        PatientEntity patientEntity = jwtTokenReCheck.checkJWTAndSessionPatient(request);
        if(patientEntity==null)
        {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        if (file.isEmpty()) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("No PDF uploaded");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        if (!"application/pdf".equals(file.getContentType())) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("File is not a PDF");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);

        }
        String randomUUID = UUID.randomUUID().toString();
//        String fileName = "Report-" + patientEntity.getFirstName() + "-" + randomUUID;
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String contentType = file.getContentType();
        InputStream inputStream = file.getInputStream();
        long filesize = file.getSize();
        byte[] encryptedBytes = encryptFile.encryptFile(inputStream);

        // Create an InputStream from the encrypted content
        InputStream encryptedInputStream = new ByteArrayInputStream(encryptedBytes);
        if(!awsServiceImplementation.uploadFile("adityavit36",fileName, (long)encryptedBytes.length, contentType, encryptedInputStream)){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Unexpected Error Please upload Again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        //TODO: work to be done
        Optional<DoctorEntity> doctor = doctorRepository.findById(id);
        String doctorEmail = doctor.get().getEmail();

        ConnectionEntity connectionEntity = connection.findConnection(doctorEmail, patientEntity.getEmail());

        if(!doctorEmail.isEmpty() && connectionEntity == null) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Doctor not Found, please try again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        ReportEntity reportEntity = report.addReport(fileName, patientEntity, connectionEntity);
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
        String bucketName = "adityavit36";// Download the encrypted file content
        val encryptedBody = awsServiceImplementation.downloadFile(bucketName,reportEntity.getFileName()); // Decrypt the file content
        byte[] decryptedBytes = encryptFile.decryptFile(encryptedBody.toByteArray());
        patient.setLastAccessTime(patientEntity.getEmail());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", reportEntity.getFileName());
        headers.setContentType(FileTypeEnum.fromFilename(reportEntity.getFileName()));
        // Return decrypted file content as response body
        return ResponseEntity.ok()
                .headers(headers)
                .body(decryptedBytes);
//      return ResponseEntity.ok().body(decryptedBytes);
    }

}
