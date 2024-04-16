package com.example.server.doctor;

import com.example.server.dto.response.DoctorDetailsResponse;
import com.example.server.dto.response.DoctorStatus;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.emailOtpPassword.PasswordUtil;
import com.example.server.hospitalSpecialization.HospitalSpecializationEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;

    private final PasswordUtil passwordUtil;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final EmailSender emailSender;

    public DoctorService(DoctorRepository doctorRepository, PasswordUtil passwordUtil, BCryptPasswordEncoder bCryptPasswordEncoder, EmailSender emailSender) {
        this.doctorRepository = doctorRepository;
        this.passwordUtil = passwordUtil;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailSender = emailSender;
    }

    public void addDoctor(DoctorEntity newDoctor) {
        doctorRepository.save(newDoctor);
    }

    public void setJwtToken(String jwtToken, String email) {
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        doctor.setJwtToken(jwtToken);
        doctorRepository.save(doctor);
    }

    public void setLastAccessTime(String email) {
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        doctor.setLastAccessTime(LocalDateTime.now());
        doctorRepository.save(doctor);
    }

    public DoctorEntity checkJWT(String email, String jwtToken) {
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        if(doctor==null || !Objects.equals(doctor.getJwtToken(), jwtToken)){
            return null;
        }
        return checkAccessTime(doctor.getEmail());
    }

    private DoctorEntity checkAccessTime(String email) {
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        if (doctor.getLastAccessTime().plusHours(2).isBefore(LocalDateTime.now())) {
            return null;
        }
        return doctor;
    }


    public DoctorEntity findDoctorByEmail(String email){
        return doctorRepository.findDoctorEntitiesByEmail(email);
    }

    public DoctorEntity registerNewDoctor(String firstName, String lastName,  String email, String registrationId, String degree, Long phoneNumber){
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        if(doctor != null){
            return null;
        }

        DoctorEntity newDoctor = new DoctorEntity();
        newDoctor.setFirstName(firstName);
        newDoctor.setLastName(lastName);
        newDoctor.setEmail(email);
        newDoctor.setRegistrationId(registrationId);
        newDoctor.setDegree(degree);
        newDoctor.setPhoneNumber(phoneNumber);
        String randomPassword = passwordUtil.generateRandomPassword();
        newDoctor.setPassword(bCryptPasswordEncoder.encode(randomPassword));
        newDoctor.setFirstTimeLogin(false);
        newDoctor.setRole("ROLE_doctor");

        doctorRepository.save(newDoctor);

        emailSender.sendMailWithPassword(
                newDoctor.getEmail(), newDoctor.getFirstName(), randomPassword
        );
        return doctorRepository.findDoctorEntitiesByEmail(newDoctor.getEmail());

    }

    public DoctorEntity verifyDoctor(String email, String otp){
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        if(doctor==null){
            return null;
        }
        else if(doctor.getOtp().equals(otp) && Duration.between(doctor.getOtpGeneratedTime(),
                LocalDateTime.now()).getSeconds()<(2*60)) {
            doctor.setActiveStatus(1);
            return doctorRepository.save(doctor);
        }
        else{
            return null;
        }
    }

    public boolean checkDoctor(String email){
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        return doctor != null;
    }

    public DoctorEntity doctorDetails(String email){
        return doctorRepository.findDoctorEntitiesByEmail(email);
    }

    public DoctorEntity updateOtp(String otp, String email){
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        doctor.setOtp(otp);
        doctor.setOtpGeneratedTime(LocalDateTime.now());
        return doctorRepository.save(doctor);
    }

    //to get the doctors of same department
    public List<String> getDoctorsFromSameSpecialization(HospitalSpecializationEntity hospitalSpecialization) {
        return doctorRepository.findNamesByHospitalSpecialization(hospitalSpecialization);
    }

    public List<DoctorDetailsResponse> getAllDoctorDetails() {
        List<DoctorEntity> allDoctors = doctorRepository.findAll();
        return allDoctors.stream()
                .map(this::mapToDoctorDetailsResponse)
                .collect(Collectors.toList());
    }

    private DoctorDetailsResponse mapToDoctorDetailsResponse(DoctorEntity doctorEntity) {
        DoctorDetailsResponse doctorDetailsResponse = new DoctorDetailsResponse();
        doctorDetailsResponse.setFirstName(doctorEntity.getFirstName());
        doctorDetailsResponse.setLastName(doctorEntity.getLastName());
        doctorDetailsResponse.setHospitalName(doctorEntity.getHospitalSpecialization().getHospital().getHospitalName());
        doctorDetailsResponse.setSpecialization(doctorEntity.getHospitalSpecialization().getSpecialization().getName());
        doctorDetailsResponse.setDoctorEmail(doctorEntity.getEmail());
        doctorDetailsResponse.setDegree(doctorEntity.getDegree());
        doctorDetailsResponse.setImageUrl(doctorEntity.getImageUrl());
        return doctorDetailsResponse;
    }

    public List<DoctorStatus> getDoctorStatus() {
        List<DoctorEntity> doctors = doctorRepository.findAll();
        return doctors.stream()
                .map(this::mapToDoctorStatus)
                .collect(Collectors.toList());
    }

    private DoctorStatus mapToDoctorStatus(DoctorEntity doctorEntity) {
        DoctorStatus doctorStatus = new DoctorStatus();
        doctorStatus.setDoctorId(doctorEntity.getId());
        doctorStatus.setEmail(doctorEntity.getEmail());
        doctorStatus.setDegree(doctorEntity.getDegree());
        doctorStatus.setHospitalName(doctorEntity.getHospitalSpecialization().getHospital().getHospitalName());
        doctorStatus.setFirstName(doctorEntity.getFirstName());
        doctorStatus.setLastName(doctorEntity.getLastName());
        doctorStatus.setStatus(doctorEntity.getActiveStatus() == 1 ? "Active" : "Inactive");
        return doctorStatus;
    }

    public List<DoctorEntity> getDoctorsUnder(DoctorEntity doctorEntity) {
        return doctorRepository.findDoctorEntitesBySeniorDoctor(doctorEntity);
    }

    public DoctorEntity updatePassword(String email, String password) {
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        doctor.setPassword(bCryptPasswordEncoder.encode(password));
        return doctorRepository.save(doctor);
    }
}
