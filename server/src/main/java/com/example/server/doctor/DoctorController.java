package com.example.server.doctor;

import com.example.server.dto.request.EmailRequest;
import com.example.server.dto.request.LoginUserRequest;
import com.example.server.dto.request.VerifyEmailRequest;
import com.example.server.dto.response.DepartmentDto;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.hospitalSpecialization.HospitalSpecializationEntity;
import com.example.server.patient.PatientController;
import com.example.server.patient.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctor")
@CrossOrigin
public class DoctorController {
    private final DoctorService doctor;

    private final EmailSender emailSender;

    public DoctorController(DoctorService doctorService, DoctorService doctor, EmailSender emailSender) {
        this.doctor = doctor;
        this.emailSender = emailSender;
    }

    @PostMapping("/login")
    ResponseEntity<DoctorEntity> loginDoctor(@RequestBody VerifyEmailRequest body){
        DoctorEntity newDoctor = doctor.verifyDoctor(
                body.getUser().getEmail(),
                body.getUser().getOtp()
        );
        return ResponseEntity.ok(newDoctor);
    }

    @PostMapping("/loginotp")
    ResponseEntity<Void> loginDoctoremail(@RequestBody LoginUserRequest body){
        if(!doctor.checkDoctor(body.getUser().getEmail())){
            throw new PatientService.PatientNotFoundException();
        }
        DoctorEntity currentDoctor = doctor.doctorDetails(body.getUser().getEmail());
        String otp = emailSender.sendOtpEmail(
                body.getUser().getEmail(),
                currentDoctor.getFirstName()
        );
        DoctorEntity doctor1 = doctor.updateOtp(otp, currentDoctor.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/departmentDetails")
    public ResponseEntity<DepartmentDto> getDepartmentOfDoctorId(@RequestBody EmailRequest emailRequest) {
        DoctorEntity doctorEntity = doctor.findDoctorByEmail(emailRequest.getEmail());
        if(doctorEntity==null){
            throw new PatientController.UnexpectedErrorException();
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


}
