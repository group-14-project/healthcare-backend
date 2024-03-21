package com.example.server.hospital;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<HospitalEntity, Integer>{
    HospitalEntity findHospitalEntitiesById(Integer id);
    Optional<HospitalEntity> findByEmail(String email);

}
