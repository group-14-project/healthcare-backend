package com.example.server.hospital;
import com.example.server.connection.ConnectionEntity;
import com.example.server.connection.ConnectionService;
import com.example.server.consultation.ConsultationService;
import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorRepository;
import com.example.server.doctor.DoctorService;
import com.example.server.dto.request.*;
import com.example.server.dto.response.*;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.errorOrSuccessMessageResponse.ErrorMessage;
import com.example.server.errorOrSuccessMessageResponse.SuccessMessage;
import com.example.server.hospitalSpecialization.HospitalSpecializationEntity;
import com.example.server.hospitalSpecialization.HospitalSpecializationService;
import com.example.server.jwtToken.JWTService;
import com.example.server.jwtToken.JWTTokenReCheck;
import com.example.server.patient.PatientService;
import com.example.server.reviews.ReviewService;
import com.example.server.specialization.SpecializationEntity;
import com.example.server.specialization.SpecializationService;
import com.example.server.webSocket.DoctorStatusScheduler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/hospital")
@CrossOrigin
public class HospitalController
{
    private final HospitalService hospital;
    private final DoctorService doctorService;
    private final EmailSender emailSender;
    private final ConnectionService connection;
    private final SpecializationService specializationService;
    private final HospitalSpecializationService hospitalSpecialization;
    private final DoctorRepository doctorRepository;
    private final JWTService jwtService;
    private final JWTTokenReCheck jwtTokenReCheck;
    private final ReviewService review;
    private final ConsultationService consultation;
    private final DoctorStatusScheduler doctorStatusScheduler;
    public HospitalController(HospitalService hospitalService, HospitalSpecializationService hospitalSpecialization, DoctorService doctorService, EmailSender emailSender, ConnectionService connection, SpecializationService specializationService, DoctorRepository doctorRepository, JWTService jwtService, JWTTokenReCheck jwtTokenReCheck, ReviewService review, ConsultationService consultation, DoctorStatusScheduler doctorStatusScheduler)
    {
        this.hospital = hospitalService;
        this.hospitalSpecialization = hospitalSpecialization;
        this.doctorService = doctorService;
        this.emailSender = emailSender;
        this.connection = connection;
        this.specializationService = specializationService;
        this.doctorRepository = doctorRepository;
        this.jwtService = jwtService;
        this.jwtTokenReCheck = jwtTokenReCheck;
        this.review = review;
        this.consultation = consultation;
        this.doctorStatusScheduler = doctorStatusScheduler;
    }

    //JWT Token done
    @PostMapping("/login")
    ResponseEntity<?> loginHospital(@RequestBody VerifyEmailRequest body)
    {
        HospitalEntity newHospital = hospital.verifyHospital(
                body.getUser().getEmail(),
                body.getUser().getOtp()
        );
        if(newHospital==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Wrong OTP or Wrong Email");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        List<DoctorDetailsResponse> doctors=hospital.allDoctorsOfHospital(newHospital);
        List<ConnectionEntity> connectionEntities = connection.findAllConnectionByHospital(newHospital);
        List<ViewReviewsResponse> viewReviewsResponses = review.viewReviewsByConnection(connectionEntities);
        List<EachDayCount> eachDayCounts=consultation.sendEachDayCount(connectionEntities);
        List<String> specialization = specializationService.findAll();

        HospitalResponse hospitalResponse=new HospitalResponse();
        hospitalResponse.setHospitalName(newHospital.getHospitalName());
        hospitalResponse.setAddress(newHospital.getAddress());
        hospitalResponse.setEmail(newHospital.getEmail());
        hospitalResponse.setFirstTimeLogin(newHospital.isFirstTimeLogin());
        hospitalResponse.setCity(newHospital.getCity());
        hospitalResponse.setDoctors(doctors);
        hospitalResponse.setReviewsResponses(viewReviewsResponses);
        hospitalResponse.setEachDayCounts(eachDayCounts);
        hospitalResponse.setSpecialization(specialization);


        String jwtToken = jwtService.createJwt(newHospital.getEmail(), newHospital.getRole());
        hospital.setJwtToken(jwtToken, newHospital.getEmail());
        hospital.setLastAccessTime(newHospital.getEmail());

        HttpHeaders headers = new HttpHeaders();
        ResponseCookie cookie = ResponseCookie.from("jwtToken", jwtToken)
                .httpOnly(true)
                .path("/") // Set the cookie path as per your requirements
                .maxAge(36000) // Set the cookie expiration time in seconds
                .build();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        headers.setBearerAuth(jwtToken);

        return ResponseEntity.ok().headers(headers).body(hospitalResponse);
    }

    @PostMapping("/loginotp")
    ResponseEntity<Void> loginHospitalemail(@RequestBody LoginUserRequest body)
    {
        if(!hospital.checkHospital(body.getUser().getEmail())){
            throw new PatientService.PatientNotFoundException();
        }
        HospitalEntity currentHospital= hospital.hospitalDetails(body.getUser().getEmail());
        String otp = emailSender.sendOtpEmail(
                body.getUser().getEmail(),
                currentHospital.getHospitalName()
        );
        HospitalEntity hospital1 = hospital.updateOtp(otp, currentHospital.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/updatePassword")
    ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateRequest body, HttpServletRequest request)
    {
        HospitalEntity hospitalEntity = jwtTokenReCheck.checkJWTAndSessionHospital(request);
        if(hospitalEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        HospitalEntity hosp=hospital.updatePassword(hospitalEntity.getEmail(), body.getPassword());
        HospitalResponse hospitalResponse=new HospitalResponse();
        hospitalResponse.setHospitalName(hosp.getHospitalName());
        hospitalResponse.setAddress(hosp.getAddress());
        hospitalResponse.setEmail(hosp.getEmail());
        hospitalResponse.setFirstTimeLogin(hosp.isFirstTimeLogin());
        hospitalResponse.setCity(hosp.getCity());
        List<DoctorDetailsResponse> doctors=hospital.allDoctorsOfHospital(hosp);
        hospitalResponse.setDoctors(doctors);
        return ResponseEntity.ok(hospitalResponse);
    }

    @PostMapping("/addDoctor")
    ResponseEntity<?> addDoctor(@RequestBody DoctorDto doctor, HttpServletRequest request) throws IOException {
        HospitalEntity hospitalEntity = jwtTokenReCheck.checkJWTAndSessionHospital(request);
        if(hospitalEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        HospitalSpecializationEntity hsc=hospitalSpecialization.getDoctorSpecialization(doctor, hospitalEntity.getEmail());
        if(hsc==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Please Enter Valid Specialization");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        DoctorEntity doc=doctorService.registerNewDoctor(doctor.getFirstName(),doctor.getLastName(),doctor.getDoctorEmail(),doctor.getRegistrationId(), doctor.getDegree(), doctor.getPhoneNumber());
        doc.setHospitalSpecialization(hsc);
        doctorRepository.save(doc);
        doctorStatusScheduler.sendDoctorStatusUpdate();

        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("Doctor has been registered successfully");
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping("/addSpecialization")
    ResponseEntity<?>  registerNewSpecialization(@RequestBody AddSpecializationRequest body, HttpServletRequest request) throws IOException {
        HospitalEntity hospitalEntity = jwtTokenReCheck.checkJWTAndSessionHospital(request);
        if(hospitalEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        SpecializationEntity specializationEntity = specializationService.getSpecializationId(body.getSpecializationName());
        if(specializationEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Specialization Name not correct");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        HospitalSpecializationEntity hospitalSpecializationEntity = hospitalSpecialization.findByHospitalAndSpecialization(hospitalEntity, specializationEntity);
        if(hospitalSpecializationEntity!=null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Data not correct or specialization already exits");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        DoctorEntity newDoctor = doctorService.registerNewDoctor(
                body.getDoctorFirstName(),
                body.getDoctorLastName(),
                body.getDoctorEmail(),
                body.getDoctorRegistrationId(),
                body.getDegree(),
                body.getPhoneNumber()
        );
        if(newDoctor==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Doctor already exists");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        HospitalSpecializationEntity newSpecialization = hospitalSpecialization.registerNewSpecialization(
                body.getSpecializationName(),
                hospitalEntity.getEmail(),
                body.getDoctorEmail()
        );

        if(newSpecialization==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Data not correct or specialization already exits");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        newDoctor.setHospitalSpecialization(newSpecialization);
        newDoctor.setRole("ROLE_seniorDoctor");
        newDoctor.setSenior(true);
        doctorService.addDoctor(newDoctor);

        doctorStatusScheduler.sendDoctorStatusUpdate();
        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("Department has been registered successfully");
        return ResponseEntity.ok(successMessage);
    }

    @GetMapping("/departments")
    public ResponseEntity<?> viewDepartments(HttpServletRequest request){
        HospitalEntity hospitalEntity = jwtTokenReCheck.checkJWTAndSessionHospital(request);
        if(hospitalEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        List<HospitalSpecializationEntity> hospitalSpecializationEntities = hospitalSpecialization.getSpecializationByHospital(hospitalEntity);
        List<DepartmentDto> departmentDtos = hospitalSpecialization.getDepartmentDoctors(hospitalSpecializationEntities);
        return ResponseEntity.ok(departmentDtos);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request){
        HospitalEntity hospitalEntity = jwtTokenReCheck.checkJWTAndSessionHospital(request);
        if (hospitalEntity == null) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("You have been logged out");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        hospital.expireJWTfromTable(hospitalEntity.getEmail());
        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("You have been logged out");
        return ResponseEntity.ok(successMessage);
    }

}
