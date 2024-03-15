package com.example.server.doctor;

import org.springframework.stereotype.Service;

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


}
