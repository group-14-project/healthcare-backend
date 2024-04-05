package com.example.server.doctor;

import com.example.server.dto.response.DoctorDetailsResponse;
import com.example.server.dto.response.DoctorStatus;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.emailOtpPassword.PasswordUtil;
import com.example.server.hospitalSpecialization.HospitalSpecializationEntity;
import com.example.server.patient.PatientService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
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


    public static class DoctorExists extends SecurityException{
        public DoctorExists(){
            super("Doctor already registered");
        }
    }

    public DoctorEntity findDoctorByEmail(String email){
        return doctorRepository.findDoctorEntitiesByEmail(email);
    }

    public DoctorEntity registerNewDoctor(String firstName, String lastName,  String email, String registrationId, String degree, Long phoneNumber){
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        if(doctor != null){
            throw new DoctorService.DoctorExists();
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
        doctorRepository.save(newDoctor);

        emailSender.sendMailWithPassword(
                newDoctor.getEmail(), newDoctor.getFirstName(), randomPassword
        );
        return doctorRepository.findDoctorEntitiesByEmail(newDoctor.getEmail());

    }

    public DoctorEntity verifyDoctor(String email, String otp){
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        if(doctor==null){
            throw new PatientService.PatientNotFoundException();
        }
        else if(doctor.getOtp().equals(otp) && Duration.between(doctor.getOtpGeneratedTime(),
                LocalDateTime.now()).getSeconds()<(2*60)) {
            return doctor;
        }
        else{
            throw new PatientService.OtpNotVerifiedException();
        }
    }

    public boolean checkDoctor(String email){
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        return doctor != null;
    }

    public DoctorEntity doctorDetails(String email){
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        if(doctor==null){
            throw new PatientService.PatientNotFoundException();
        }
        return doctor;
    }

    public DoctorEntity updateOtp(String otp, String email){
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        if(doctor==null){
            throw new PatientService.PatientNotFoundException();
        }
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

}
