package com.example.server.hospital;
import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorRepository;
import com.example.server.doctor.DoctorService;
import com.example.server.dto.request.DoctorDto;
import com.example.server.dto.request.LoginUserRequest;
import com.example.server.dto.request.VerifyEmailRequest;
import com.example.server.dto.response.ApiResponse;
import com.example.server.dto.response.DoctorDetailsResponse;
import com.example.server.dto.response.HospitalResponse;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.hospitalSpecialization.HospitalSpecializationEntity;
import com.example.server.hospitalSpecialization.HospitalSpecializationService;
import com.example.server.patient.PatientService;
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

    public HospitalController(HospitalService hospitalService, HospitalSpecializationService hospitalSpecialization, DoctorService doctorService, EmailSender emailSender, DoctorRepository doctorRepository)
    {
        this.hospital = hospitalService;
        this.hospitalSpecialization = hospitalSpecialization;
        this.doctorService = doctorService;
        this.emailSender = emailSender;
        this.doctorRepository = doctorRepository;
    }

    @PostMapping("/login")
    ResponseEntity<HospitalResponse> loginPatient(@RequestBody VerifyEmailRequest body)
    {
        HospitalEntity newHospital = hospital.verifyHospital(
                body.getUser().getEmail(),
                body.getUser().getOtp()
        );
        HospitalResponse hospitalResponse=new HospitalResponse();
        hospitalResponse.setHospitalName(newHospital.getHospitalName());
        hospitalResponse.setAddress(newHospital.getAddress());
        hospitalResponse.setEmail(newHospital.getEmail());
        hospitalResponse.setFirstTimeLogin(newHospital.isFirstTimeLogin());
        hospitalResponse.setCity(newHospital.getCity());
        List<DoctorDetailsResponse> doctors=hospital.allDoctorsOfHospital(newHospital);
        hospitalResponse.setDoctors(doctors);
        return ResponseEntity.ok(hospitalResponse);
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
    ResponseEntity<HospitalResponse> updatePassword(@RequestBody LoginUserRequest data)
    {
        HospitalEntity hosp=hospital.updatePassword(data);
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
    ResponseEntity<ApiResponse> addDoctor(@RequestBody DoctorDto doctor)
    {
        HospitalSpecializationEntity hsc=hospitalSpecialization.getDoctorSpecialization(doctor);
        DoctorEntity doc=doctorService.registerNewDoctor(doctor.getFirstName(),doctor.getLastName(),doctor.getDoctorEmail(),doctor.getRegistrationId(), doctor.getDegree(), doctor.getPhoneNumber());
        doc.setHospitalSpecialization(hsc);
        doctorRepository.save(doc);
        return new ResponseEntity<>(new ApiResponse("Doctor added successfully",true),HttpStatus.OK);
    }

}
