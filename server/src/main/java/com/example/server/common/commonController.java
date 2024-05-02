package com.example.server.common;

import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorService;
import com.example.server.dto.request.ChangePassword;
import com.example.server.dto.request.EmailRole;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.errorOrSuccessMessageResponse.ErrorMessage;
import com.example.server.errorOrSuccessMessageResponse.SuccessMessage;
import com.example.server.hospital.HospitalEntity;
import com.example.server.hospital.HospitalService;
import com.example.server.patient.PatientEntity;
import com.example.server.patient.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@CrossOrigin
public class commonController {
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final HospitalService hospitalService;
    private final EmailSender emailSender;

    public commonController(PatientService patientService, DoctorService doctorService, HospitalService hospitalService, EmailSender emailSender) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.hospitalService = hospitalService;
        this.emailSender = emailSender;
    }

    @PostMapping("/forgotPassword")
    ResponseEntity<?> getOtpToChangePassword(@RequestBody EmailRole body){
        if(Objects.equals(body.getRole(), "patient")){
            PatientEntity patientEntity = patientService.patientDetails(body.getEmail());
            if(patientEntity==null){
                ErrorMessage errorMessage = new ErrorMessage();
                errorMessage.setErrorMessage("Patient with this emailID does not exist");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            }
            String otp = emailSender.sendOtpEmail(body.getEmail(), patientEntity.getFirstName());
            patientService.updateOtp(otp, body.getEmail());
        }
        else if(Objects.equals(body.getRole(), "doctor")){
            DoctorEntity doctorEntity = doctorService.findDoctorByEmail(body.getEmail());
            if(doctorEntity==null){
                ErrorMessage errorMessage = new ErrorMessage();
                errorMessage.setErrorMessage("Doctor with this emailID does not exist");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            }
            String otp = emailSender.sendOtpEmail(body.getEmail(), doctorEntity.getFirstName());
            doctorService.updateOtp(otp, body.getEmail());
        }
        else if(Objects.equals(body.getRole(), "hospital")){
            HospitalEntity hospitalEntity = hospitalService.hospitalDetails(body.getEmail());
            if(hospitalEntity==null){
                ErrorMessage errorMessage = new ErrorMessage();
                errorMessage.setErrorMessage("Hospital with this emailID does not exist");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            }
            String otp = emailSender.sendOtpEmail(body.getEmail(), hospitalEntity.getHospitalName());
            hospitalService.updateOtp(otp, body.getEmail());
        }
        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("Otp has been sent to the email");
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping("/changePassword")
    ResponseEntity<?> changePassword(@RequestBody ChangePassword body){
        if(Objects.equals(body.getRole(), "patient")){
            PatientEntity patientEntity = patientService.patientDetails(body.getEmail());
            if(patientEntity==null){
                ErrorMessage errorMessage = new ErrorMessage();
                errorMessage.setErrorMessage("Patient with this emailID does not exist");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            }
            if(patientService.verifyPatient(patientEntity.getEmail(), body.getOtp())==null){
                ErrorMessage errorMessage = new ErrorMessage();
                errorMessage.setErrorMessage("You have entered incorrect OTP");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            }
            patientService.passwordChange(body.getPassword(), patientEntity.getEmail());
        }
        else if(Objects.equals(body.getRole(), "doctor")){
            DoctorEntity doctorEntity = doctorService.findDoctorByEmail(body.getEmail());
            if(doctorEntity==null){
                ErrorMessage errorMessage = new ErrorMessage();
                errorMessage.setErrorMessage("Doctor with this emailID does not exist");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            }
            if(doctorService.verifyDoctor(doctorEntity.getEmail(), body.getOtp())==null){
                ErrorMessage errorMessage = new ErrorMessage();
                errorMessage.setErrorMessage("You have entered incorrect OTP");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            }
            doctorService.passwordChange(body.getPassword(), doctorEntity.getEmail());
        }
        else if(Objects.equals(body.getRole(), "hospital")){
            HospitalEntity hospitalEntity = hospitalService.hospitalDetails(body.getEmail());
            if(hospitalEntity==null){
                ErrorMessage errorMessage = new ErrorMessage();
                errorMessage.setErrorMessage("Hospital with this emailID does not exist");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            }
            if(hospitalService.verifyHospital(hospitalEntity.getEmail(), body.getOtp())==null){
                ErrorMessage errorMessage = new ErrorMessage();
                errorMessage.setErrorMessage("You have entered incorrect OTP");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            }
            hospitalService.passwordChange(body.getPassword(), hospitalEntity.getEmail());
        }
        SuccessMessage successMessage = new SuccessMessage();
        successMessage.setSuccessMessage("Password has been changed successfully");
        return ResponseEntity.ok(successMessage);
    }


}
