package com.example.server.doctor;

import com.example.server.hospital.HospitalEntity;
import com.example.server.hospitalSpecialization.HospitalSpecializationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<DoctorEntity, Integer> {
    DoctorEntity findDoctorEntitiesByEmail(String email);

    @Query("select concat(d.firstName, ' ', d.lastName) from DoctorEntity d " +
    "WHERE d.hospitalSpecialization = :hospitalSpecialization")
    List<String> findNamesByHospitalSpecialization(HospitalSpecializationEntity hospitalSpecialization);

    @Query("select c from DoctorEntity c where c.hospitalSpecialization = :specializationEntity")
    List<DoctorEntity> findAllBySpecialization(HospitalSpecializationEntity specializationEntity);

    @Query("select c from DoctorEntity c where c.id = :id")
    DoctorEntity findByDoctorId(Integer id);

    @Query("select c from DoctorEntity c where c.hospitalSpecialization = :hospitalSpecializationEntity and c.id in :doctorIds")
    List<DoctorEntity> getDoctorsUnderInCall(HospitalSpecializationEntity hospitalSpecializationEntity, List<Integer> doctorIds);
}
