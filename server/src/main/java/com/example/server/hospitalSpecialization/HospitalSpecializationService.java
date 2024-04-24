package com.example.server.hospitalSpecialization;

import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorService;
import com.example.server.dto.request.DoctorDto;
import com.example.server.dto.response.DepartmentDto;
import com.example.server.dto.response.DoctorDetailsResponse;
import com.example.server.hospital.HospitalEntity;
import com.example.server.hospital.HospitalService;
import com.example.server.specialization.SpecializationEntity;
import com.example.server.specialization.SpecializationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

        HospitalSpecializationEntity currSpecialization = hospitalSpecializationRepository.findByHospitalIdAndSpecializationId(hospitalEntity, specializationEntity);
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
        return hospitalSpecializationRepository.findByHospitalIdAndSpecializationId(hospital,specialization);
    }

    public List<HospitalSpecializationEntity> getSpecializationByHospital(HospitalEntity hospital) {
        return hospitalSpecializationRepository.findAllByHospital(hospital);
    }

    public HospitalSpecializationEntity findByHospitalAndSpecialization(HospitalEntity hospitalEntity, SpecializationEntity specializationEntity) {
        return hospitalSpecializationRepository.findByHospitalIdAndSpecializationId(hospitalEntity, specializationEntity);
    }

    public List<DepartmentDto> getDepartmentDoctors(List<HospitalSpecializationEntity> hospitalSpecializationEntities) {
        List<DepartmentDto> departmentDtos = new ArrayList<>();

        for (HospitalSpecializationEntity hospitalSpecialization : hospitalSpecializationEntities) {
            DoctorDetailsResponse seniorDoctor = new DoctorDetailsResponse();
            seniorDoctor.setImageUrl(hospitalSpecialization.getHeadDoctor().getImageUrl());
            seniorDoctor.setDoctorEmail(hospitalSpecialization.getHeadDoctor().getEmail());
            seniorDoctor.setDegree(hospitalSpecialization.getHeadDoctor().getDegree());
            seniorDoctor.setSpecialization(hospitalSpecialization.getSpecialization().getName());
            seniorDoctor.setFirstName(hospitalSpecialization.getHeadDoctor().getFirstName());
            seniorDoctor.setLastName(hospitalSpecialization.getHeadDoctor().getLastName());
            seniorDoctor.setHospitalName(hospitalSpecialization.getHospital().getHospitalName());

            String specialization = hospitalSpecialization.getSpecialization().getName();


            List<DoctorDetailsResponse> doctors = doctorService.getDoctorsInSpecialization(hospitalSpecialization);

            DepartmentDto departmentDto = new DepartmentDto(seniorDoctor, specialization, doctors);
            departmentDtos.add(departmentDto);
        }
        return departmentDtos;
    }
}
