package com.example.server.hospitalSpecialization;

import com.example.server.hospital.HospitalEntity;
import com.example.server.specialization.SpecializationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HospitalSpecializationRepository extends JpaRepository<HospitalSpecializationEntity, Integer> {

    @Query("select c from HospitalSpecializationEntity c where c.hospital = :hospital and c.specialization = :specialization")
    HospitalSpecializationEntity findByHospitalIdAndSpecializationId(HospitalEntity hospital, SpecializationEntity specialization);

    @Query("select c from HospitalSpecializationEntity c where c.hospital = :hospital")
    List<HospitalSpecializationEntity> findAllByHospital(HospitalEntity hospital);
}
