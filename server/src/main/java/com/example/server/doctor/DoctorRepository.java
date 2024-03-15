package com.example.server.doctor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<DoctorEntity, Integer> {
    DoctorEntity findDoctorEntitiesByEmail(String email);
}
