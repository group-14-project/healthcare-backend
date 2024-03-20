package com.example.server.SuperAdmin;
import com.example.server.dto.request.HospitalRequestDto;
import com.example.server.dto.request.ResponseDto;
import com.example.server.emailOtp.EmailSender;
import com.example.server.emailOtp.OtpUtil;
import com.example.server.hospital.HospitalEntity;
import com.example.server.hospital.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class SuperAdminService
{
    @Autowired
    HospitalRepository hospitalRepo;

    @Autowired
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailSender emailSender;

    public SuperAdminService(BCryptPasswordEncoder bCryptPasswordEncoder, OtpUtil otpUtil, EmailSender emailSender)
    {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailSender = emailSender;
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
        hospital.setRole(hospitalRequestDto.getRole());
        //getting the role from the response and putting it in the hospital
        hospital.setEmailVerify(false);
        //at the starting this will be false until we will not verify it
        hospital.setPinCode(hospitalRequestDto.getPinCode());
        //getting the pin code from the response and putting it in the hospital
        //this we are getting the otp and putting it in the hospital
        String randomPassword=generateRandomPassword();
        hospital.setPassword(bCryptPasswordEncoder.encode(randomPassword));
        //getting the password from the response and encoding it and setting it in the hospital
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
    private String generateRandomPassword()
    {
        int passwordLength = 5;
        StringBuilder sb = new StringBuilder(passwordLength);
        Random random = new Random();
        String allowedCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < passwordLength; i++)
        {
            int randomIndex = random.nextInt(allowedCharacters.length());
            sb.append(allowedCharacters.charAt(randomIndex));
        }
        return sb.toString();
    }

}