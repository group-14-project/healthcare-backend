package com.example.server.patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
@Repository
public interface PatientRepository extends JpaRepository<PatientEntity, Integer> {
    PatientEntity findPatientEntitiesByEmail(String email);
    List<PatientEntity> findByDeletionTimeBefore(LocalDate thresholdDate);
}
