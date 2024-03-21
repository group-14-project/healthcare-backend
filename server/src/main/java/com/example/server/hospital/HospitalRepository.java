package com.example.server.hospital;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalRepository extends JpaRepository<HospitalEntity, Integer>{
    HospitalEntity findHospitalEntitiesById(Integer id);
    HospitalEntity findByEmail(String email);

}
