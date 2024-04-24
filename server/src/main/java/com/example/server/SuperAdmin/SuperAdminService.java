package com.example.server.SuperAdmin;
import com.example.server.dto.request.HospitalRequestDto;
import com.example.server.dto.request.ResponseDto;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.emailOtpPassword.OtpUtil;
import com.example.server.emailOtpPassword.PasswordUtil;
import com.example.server.hospital.HospitalEntity;
import com.example.server.hospital.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SuperAdminService
{
    @Autowired
    HospitalRepository hospitalRepo;

    @Autowired
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailSender emailSender;

    private final PasswordUtil passwordUtil;

    public SuperAdminService(BCryptPasswordEncoder bCryptPasswordEncoder, OtpUtil otpUtil, EmailSender emailSender, PasswordUtil passwordUtil)
    {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailSender = emailSender;
        this.passwordUtil = passwordUtil;
    }

    public ResponseDto registerHospital(HospitalRequestDto hospitalRequestDto)
    {
        ResponseDto response = new ResponseDto();
        HospitalEntity hospital = new HospitalEntity();
        //this we have created a new hospital
        //now time to put data into the hospital that we get from the response
        hospital.setHospitalName(hospitalRequestDto.getHospitalName());
        //getting the hospital name from the response and putting it in the hospital
        hospital.setCity(hospitalRequestDto.getCity());
        //getting the city name from the response and putting it in the hospital
        hospital.setAddress(hospitalRequestDto.getAddress());
        //getting the address from the response and putting it in the hospital
        hospital.setEmail(hospitalRequestDto.getEmail());
        //getting the email from the response and putting it in the hospital
        hospital.setRole("ROLE_hospital");
        //getting the role from the response and putting it in the hospital
        hospital.setEmailVerify(false);
        //at the starting this will be false until we will not verify it
        hospital.setPinCode(hospitalRequestDto.getPinCode());
        //getting the pin code from the response and putting it in the hospital
        //this we are getting the otp and putting it in the hospital
        String randomPassword=passwordUtil.generateRandomPassword();
        hospital.setPassword(bCryptPasswordEncoder.encode(randomPassword));
        //getting the password from the response and encoding it and setting it in the hospital
        hospital.setFirstTimeLogin(false);
        HospitalEntity savedHospital = hospitalRepo.save(hospital);
        //saving the hospital
        response.setHospitalName(savedHospital.getHospitalName());
        response.setEmailVerify(savedHospital.isEmailVerify());
        response.setMessage("Password sent successfully to email");
        response.setId(savedHospital.getId());
        response.setEmail(savedHospital.getEmail());
        emailSender.sendMailWithPassword(
                savedHospital.getEmail(),savedHospital.getHospitalName(),randomPassword);
        return response;
    }

}