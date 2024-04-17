package com.example.server.hospitalSpecialization;

import com.example.server.hospital.HospitalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HospitalSpecializationRepository extends JpaRepository<HospitalSpecializationEntity, Integer> {
    HospitalSpecializationEntity findByHospitalIdAndSpecializationId(Integer hospitalId, Integer specializationId);

    @Query("select c from HospitalSpecializationEntity c where c.hospital = :hospital")
    List<HospitalSpecializationEntity> findAllByHospital(HospitalEntity hospital);
}
