package com.example.server.doctor;

import com.example.server.dto.request.LoginUserRequest;
import com.example.server.dto.request.VerifyEmailRequest;
import com.example.server.emailOtp.EmailSender;
import com.example.server.patient.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


}
