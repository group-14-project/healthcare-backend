package com.example.server.hospital;
import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorRepository;
import com.example.server.doctor.DoctorService;
import com.example.server.dto.request.DoctorDto;
import com.example.server.dto.request.LoginUserRequest;
import com.example.server.dto.request.VerifyEmailRequest;
import com.example.server.dto.response.ApiResponse;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.hospitalSpecialization.HospitalSpecializationEntity;
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
    private final DoctorRepository doctorRepository;

    public HospitalController(HospitalService hospitalService, HospitalSpecializationService hospitalSpecialization, DoctorService doctorService, EmailSender emailSender, DoctorRepository doctorRepository)
    {
        this.hospital = hospitalService;
        this.hospitalSpecialization = hospitalSpecialization;
        this.doctorService = doctorService;
        this.emailSender = emailSender;
        this.doctorRepository = doctorRepository;
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

    @PostMapping("/addDoctor")
    ResponseEntity<ApiResponse> addDoctor(@RequestBody DoctorDto doctor)
    {
        HospitalSpecializationEntity hsc=hospitalSpecialization.getDoctorSpecialization(doctor);
        DoctorEntity doc=doctorService.registerNewDoctor(doctor.getFirstName(),doctor.getLastName(),doctor.getDoctorEmail(),doctor.getRegistrationId());
        doc.setHospitalSpecialization(hsc);
        doctorRepository.save(doc);
        return new ResponseEntity<>(new ApiResponse("Doctor added successfully",true),HttpStatus.OK);
    }

}
