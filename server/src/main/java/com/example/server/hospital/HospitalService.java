package com.example.server.hospital;

import org.springframework.stereotype.Service;

@Service
public class HospitalService {
    private final HospitalRepository hospitalRepository;

    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    public HospitalEntity findHospitalById(Integer id){
        return hospitalRepository.findHospitalEntitiesById(id);
    }
}
