package com.example.server.connection;

import com.example.server.doctor.DoctorEntity;
import com.example.server.dto.response.EachDayCount;
import com.example.server.patient.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConnectionRepository extends JpaRepository<ConnectionEntity, Integer> {
    ConnectionEntity findByDoctorAndPatient(DoctorEntity doctor, PatientEntity patient);

    List<ConnectionEntity> findByPatient(PatientEntity newPatient);

    List<ConnectionEntity> findByDoctor(DoctorEntity newDoctor);

    @Query("SELECT COUNT(c) FROM ConnectionEntity c WHERE c.doctor = :doctor")
    Integer countByDoctor(DoctorEntity doctor);


    @Query("select c.patient from ConnectionEntity c where c.doctor = :doctor")
    List<PatientEntity> findPatientsByDoctor(DoctorEntity doctor);

    @Query("select c from ConnectionEntity c where c.doctor in :doctorEntities")
    List<ConnectionEntity> findAllByDoctorList(List<DoctorEntity> doctorEntities);
}
