package com.example.server.hospital;
import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorRepository;
import com.example.server.doctor.DoctorService;
import com.example.server.dto.request.*;
import com.example.server.dto.response.ApiResponse;
import com.example.server.dto.response.DoctorDetailsResponse;
import com.example.server.dto.response.HospitalResponse;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.errorOrSuccessMessageResponse.ErrorMessage;
import com.example.server.errorOrSuccessMessageResponse.SuccessMessage;
import com.example.server.hospitalSpecialization.HospitalSpecializationEntity;
import com.example.server.hospitalSpecialization.HospitalSpecializationService;
import com.example.server.jwtToken.JWTService;
import com.example.server.jwtToken.JWTTokenReCheck;
import com.example.server.patient.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hospital")
@CrossOrigin
public class HospitalController
{
    private final HospitalService hospital;
    private final DoctorService doctorService;
    private final EmailSender emailSender;
    private final HospitalSpecializationService hospitalSpecialization;
    private final DoctorRepository doctorRepository;
    private final JWTService jwtService;
    private final JWTTokenReCheck jwtTokenReCheck;
    public HospitalController(HospitalService hospitalService, HospitalSpecializationService hospitalSpecialization, DoctorService doctorService, EmailSender emailSender, DoctorRepository doctorRepository, JWTService jwtService, JWTTokenReCheck jwtTokenReCheck)
    {
        this.hospital = hospitalService;
        this.hospitalSpecialization = hospitalSpecialization;
        this.doctorService = doctorService;
        this.emailSender = emailSender;
        this.doctorRepository = doctorRepository;
        this.jwtService = jwtService;
        this.jwtTokenReCheck = jwtTokenReCheck;
    }

    //JWT Token done
    @PostMapping("/login")
    ResponseEntity<?> loginPatient(@RequestBody VerifyEmailRequest body)
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
        HospitalResponse hospitalResponse=new HospitalResponse();
        hospitalResponse.setHospitalName(newHospital.getHospitalName());
        hospitalResponse.setAddress(newHospital.getAddress());
        hospitalResponse.setEmail(newHospital.getEmail());
        hospitalResponse.setFirstTimeLogin(newHospital.isFirstTimeLogin());
        hospitalResponse.setCity(newHospital.getCity());
        List<DoctorDetailsResponse> doctors=hospital.allDoctorsOfHospital(newHospital);
        hospitalResponse.setDoctors(doctors);

        String jwtToken = jwtService.createJwt(newHospital.getEmail(), newHospital.getRole());
        hospital.setJwtToken(jwtToken, newHospital.getEmail());
        hospital.setLastAccessTime(newHospital.getEmail());

        HttpHeaders headers = new HttpHeaders();
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
    ResponseEntity<?> addDoctor(@RequestBody DoctorDto doctor, HttpServletRequest request){
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

        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("Doctor has been registered successfully");
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping("/addSpecialization")
    ResponseEntity<?>  registerNewSpecialization(@RequestBody AddSpecializationRequest body, HttpServletRequest request) {
        HospitalEntity hospitalEntity = jwtTokenReCheck.checkJWTAndSessionHospital(request);
        if(hospitalEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your session has expired. Please Login again");
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
        newDoctor.setRole("ROLE_senior_doctor");
        doctorService.addDoctor(newDoctor);

        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("Department has been registered successfully");
        return ResponseEntity.ok(successMessage);
    }

}
