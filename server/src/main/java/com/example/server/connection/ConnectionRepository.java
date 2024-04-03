package com.example.server.connection;

import com.example.server.doctor.DoctorEntity;
import com.example.server.patient.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConnectionRepository extends JpaRepository<ConnectionEntity, Integer> {
    ConnectionEntity findByDoctorAndPatient(DoctorEntity doctor, PatientEntity patient);

    List<ConnectionEntity> findByPatient(PatientEntity newPatient);
}
