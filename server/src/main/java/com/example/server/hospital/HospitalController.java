package com.example.server.hospital;
import com.example.server.doctor.DoctorService;
import com.example.server.dto.request.LoginUserRequest;
import com.example.server.dto.request.VerifyEmailRequest;
import com.example.server.dto.response.ApiResponse;
import com.example.server.emailOtp.EmailSender;
import com.example.server.hospitalSpecialization.HospitalSpecializationService;
import com.example.server.patient.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/hospital")
@CrossOrigin
public class HospitalController
{

    private final HospitalService hospital;

    private final DoctorService doctorService;

    private final EmailSender emailSender;

    private final HospitalSpecializationService hospitalSpecialization;

    public HospitalController(HospitalService hospitalService, HospitalSpecializationService hospitalSpecialization, DoctorService doctorService, EmailSender emailSender)
    {
        this.hospital = hospitalService;
        this.hospitalSpecialization = hospitalSpecialization;
        this.doctorService = doctorService;
        this.emailSender = emailSender;
    }

    @PostMapping("/login")
    ResponseEntity<HospitalEntity> loginPatient(@RequestBody VerifyEmailRequest body)
    {
        HospitalEntity newHospital = hospital.verifyHospital(
                body.getUser().getEmail(),
                body.getUser().getOtp()
        );
        return ResponseEntity.ok(newHospital);
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
    ResponseEntity<ApiResponse> updatePassword(@RequestBody LoginUserRequest data)
    {
        hospital.updatePassword(data);
        return new ResponseEntity<>(new ApiResponse("Password changes successfully",true),HttpStatus.OK);
    }


//    @PostMapping("/specialization")
//    ResponseEntity<Void> registerNewSpecialization(@RequestBody AddSpecializationRequest body){
//        System.out.println("Heloooooo");
//        DoctorEntity newDoctor = doctorService.registerNewDoctor(
//                body.getSpecialization().getDoctorFirstName(),
//                body.getSpecialization().getDoctorLastName(),
//                body.getSpecialization().getDoctorEmail(),
//                body.getSpecialization().getDoctorRegistrationId()
//        );
//
//        HospitalSpecializationEntity newSpecialization = hospitalSpecialization.registerNewSpecialization(
//                body.getSpecialization().getName(),
//                body.getSpecialization().getHospitalId(),
//                body.getSpecialization().getDoctorEmail()
//        );
//
//        newDoctor.setHospitalSpecialization(newSpecialization);
//        doctorService.addDoctor(newDoctor);
//
//
//        return ResponseEntity.status(HttpStatus.CREATED).build();
//    }
}
