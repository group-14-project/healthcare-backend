package com.example.server.doctor;

import com.example.server.patient.PatientService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public void addDoctor(DoctorEntity newDoctor) {
        doctorRepository.save(newDoctor);
    }

//    public Optional<DoctorEntity> getDoc(Integer id) {
//        return doctorRepository.findById(id);
//    }

    public static class DoctorExists extends SecurityException{
        public DoctorExists(){
            super("Doctor already registered");
        }
    }

    public DoctorEntity findDoctorByEmail(String email){
        return doctorRepository.findDoctorEntitiesByEmail(email);
    }

    public DoctorEntity registerNewDoctor(String firstName, String lastName,  String email, String registrationId){
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        if(doctor != null){
            throw new DoctorService.DoctorExists();
        }

        DoctorEntity newDoctor = new DoctorEntity();
        newDoctor.setFirstName(firstName);
        newDoctor.setLastName(lastName);
        newDoctor.setEmail(email);
        newDoctor.setRegistrationId(registrationId);
        newDoctor.setPassword("123");

        return doctorRepository.save(newDoctor);
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


}
