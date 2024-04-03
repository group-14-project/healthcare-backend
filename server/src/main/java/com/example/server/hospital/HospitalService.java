package com.example.server.hospital;

import com.example.server.doctor.DoctorController;
import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorRepository;
import com.example.server.dto.request.DoctorDto;
import com.example.server.dto.request.LoginUserRequest;
import com.example.server.dto.response.DoctorDetailsResponse;
import com.example.server.hospitalSpecialization.HospitalSpecializationEntity;
import com.example.server.hospitalSpecialization.HospitalSpecializationRepository;
import com.example.server.patient.PatientService;
import com.example.server.specialization.SpecializationEntity;
import com.example.server.specialization.SpecializationRepository;
import com.example.server.specialization.SpecializationService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HospitalService {
    private final HospitalRepository hospitalRepository;

    private final PasswordEncoder passwordEncoder;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private  final SpecializationRepository specializationRepository;

    private final DoctorRepository doctorRepository;
    private final HospitalSpecializationRepository hospitalSpecializationRepository;



    public static class HospitalNotFoundException extends SecurityException{
        public HospitalNotFoundException(){
            super("Patient not found");
        }
    }


    public HospitalService(HospitalRepository hospitalRepository, PasswordEncoder passwordEncoder, BCryptPasswordEncoder bCryptPasswordEncoder, SpecializationRepository specilization, SpecializationRepository specialization, SpecializationRepository specializationRepository, DoctorRepository doctorRepository, HospitalSpecializationRepository hospitalSpecializationRepository) {
        this.hospitalRepository = hospitalRepository;
        this.passwordEncoder = passwordEncoder;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.specializationRepository = specializationRepository;
        this.doctorRepository = doctorRepository;
        this.hospitalSpecializationRepository = hospitalSpecializationRepository;
    }

    public HospitalEntity verifyHospital(String email, String otp){
        HospitalEntity hospital = hospitalRepository.findByEmail(email);
        if(hospital==null){
            throw new HospitalNotFoundException();
        }
        else if(hospital.getOtp().equals(otp) && Duration.between(hospital.getOtpGeneratedTime(),
                LocalDateTime.now()).getSeconds()<(2*60)) {
            hospital.setEmailVerify(true);
            hospitalRepository.save(hospital);
        }
        else{
            throw new PatientService.OtpNotVerifiedException();
        }
        return hospital;
    }


    public HospitalEntity updatePassword(LoginUserRequest data)
    {
        HospitalEntity hospital = this.hospitalRepository.findByEmail(data.getUser().getEmail());
        hospital.setPassword(bCryptPasswordEncoder.encode(data.getUser().getPassword()));
        hospital.setFirstTimeLogin(true);
        return this.hospitalRepository.save(hospital);
    }


    public boolean checkHospital(String email)
    {
        HospitalEntity hospital = hospitalRepository.findByEmail(email);
        return hospital != null;
    }

    public HospitalEntity hospitalDetails(String email){
        HospitalEntity hospital = hospitalRepository.findByEmail(email);
        if(hospital==null){
            throw new PatientService.PatientNotFoundException();
        }
        return hospital;
    }

    public HospitalEntity updateOtp(String otp, String email){
        HospitalEntity hospital = hospitalRepository.findByEmail(email);
        if(hospital==null){
            throw new PatientService.PatientNotFoundException();
        }
        hospital.setOtp(otp);
        hospital.setOtpGeneratedTime(LocalDateTime.now());
        return hospitalRepository.save(hospital);
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }

    public List<DoctorDetailsResponse> allDoctorsOfHospital(HospitalEntity hospital)
    {
        List<DoctorEntity> doctorEntities = doctorRepository.findAll();
        return doctorEntities.stream()
                .filter(doctor -> doctor.getHospitalSpecialization() != null && doctor.getHospitalSpecialization().getHospital().getHospitalName().equals(hospital.getHospitalName()))
                .map(this::mapToDoctorDetails)
                .collect(Collectors.toList());
    }

    private DoctorDetailsResponse mapToDoctorDetails(DoctorEntity doctorEntity)
    {
        DoctorDetailsResponse doctorDetailsResponse=new DoctorDetailsResponse();
        doctorDetailsResponse.setDoctorEmail(doctorEntity.getEmail());
        doctorDetailsResponse.setHospitalName(doctorEntity.getHospitalSpecialization().getHospital().getHospitalName());
        doctorDetailsResponse.setFirstName(doctorEntity.getFirstName());
        doctorDetailsResponse.setLastName(doctorEntity.getLastName());
        doctorDetailsResponse.setImageUrl(doctorEntity.getImageUrl());
        doctorDetailsResponse.setDegree(doctorEntity.getDegree());
        doctorDetailsResponse.setSpecialization(doctorEntity.getHospitalSpecialization().getSpecialization().getName());
        return doctorDetailsResponse;
    }


}
