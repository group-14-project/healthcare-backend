package com.example.server.hospitalSpecialization;

import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorService;
import com.example.server.dto.request.DoctorDto;
import com.example.server.hospital.HospitalEntity;
import com.example.server.hospital.HospitalService;
import com.example.server.specialization.SpecializationEntity;
import com.example.server.specialization.SpecializationService;
import org.springframework.stereotype.Service;

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

        HospitalSpecializationEntity newSpecialization = new HospitalSpecializationEntity();
        newSpecialization.setSpecialization(specializationEntity);
        newSpecialization.setHospital(hospitalEntity);
        newSpecialization.setHeadDoctor(doctorEntity);

        return hospitalSpecializationRepository.save(newSpecialization);
    }
    public HospitalSpecializationEntity getDoctorSpecialization(DoctorDto doctor)
    {
        HospitalEntity hospital=hospitalService.hospitalDetails(doctor.getHospitalEmail());
        if(hospital==null)
        {
            throw new HospitalService.HospitalNotFoundException();
        }
        SpecializationEntity specialization=specializationService.getSpecializationId(doctor.getSpecialization());
        if(specialization==null)
        {
            throw new SpecializationService.SpecializationNotFoundException();
        }
        HospitalSpecializationEntity spec=hospitalSpecializationRepository.findByHospitalIdAndSpecializationId(hospital.getId(),specialization.getId());
        return spec;
    }
}
