package com.example.server.hospitalSpecialization;

import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorService;
import com.example.server.dto.request.DoctorDto;
import com.example.server.hospital.HospitalEntity;
import com.example.server.hospital.HospitalService;
import com.example.server.specialization.SpecializationEntity;
import com.example.server.specialization.SpecializationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HospitalSpecializationService {
    private final HospitalSpecializationRepository hospitalSpecializationRepository;

    private final SpecializationService specializationService;
    private final HospitalService hospitalService;
    private final DoctorService doctorService;

    public HospitalSpecializationService(HospitalSpecializationRepository hospitalSpecializationRepository, SpecializationService specializationService, SpecializationService specializationService1, HospitalService hospitalService, DoctorService doctorService) {
        this.hospitalSpecializationRepository = hospitalSpecializationRepository;
        this.specializationService = specializationService;
        this.hospitalService = hospitalService;
        this.doctorService = doctorService;
    }

    public HospitalSpecializationEntity registerNewSpecialization(String name, String hospitalEmail, String email){
        SpecializationEntity specializationEntity = specializationService.getSpecializationId(name);
        HospitalEntity hospitalEntity = hospitalService.hospitalDetails(hospitalEmail);
        DoctorEntity doctorEntity = doctorService.findDoctorByEmail(email);

        if(specializationEntity==null || hospitalEntity==null || doctorEntity==null){
            return null;
        }

        HospitalSpecializationEntity currSpecialization = hospitalSpecializationRepository.findByHospitalIdAndSpecializationId(hospitalEntity.getId(), specializationEntity.getId());
        if(currSpecialization!=null){
            return null;
        }

        HospitalSpecializationEntity newSpecialization = new HospitalSpecializationEntity();
        newSpecialization.setSpecialization(specializationEntity);
        newSpecialization.setHospital(hospitalEntity);
        newSpecialization.setHeadDoctor(doctorEntity);

        return hospitalSpecializationRepository.save(newSpecialization);
    }
    public HospitalSpecializationEntity getDoctorSpecialization(DoctorDto doctor, String email)
    {
        HospitalEntity hospital=hospitalService.hospitalDetails(email);
        if(hospital==null) {
            return null;
        }
        SpecializationEntity specialization=specializationService.getSpecializationId(doctor.getSpecialization());
        if(specialization==null) {
            return null;
        }
        return hospitalSpecializationRepository.findByHospitalIdAndSpecializationId(hospital.getId(),specialization.getId());
    }

    public List<HospitalSpecializationEntity> getSpecializationByHospital(HospitalEntity hospital) {
        return hospitalSpecializationRepository.findAllByHospital(hospital);
    }

    public HospitalSpecializationEntity findByHospitalAndSpecialization(HospitalEntity hospitalEntity, SpecializationEntity specializationEntity) {
        return hospitalSpecializationRepository.findByHospitalIdAndSpecializationId(hospitalEntity.getId(), specializationEntity.getId());
    }
}
