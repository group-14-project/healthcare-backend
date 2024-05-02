package com.example.server.consent;

import com.example.server.connection.ConnectionEntity;
import com.example.server.doctor.DoctorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsentRepository extends JpaRepository<ConsentEntity, Integer> {

    @Query("select c from ConsentEntity c where c.connect = :connect and c.newDoctor = :newDoctor")
    ConsentEntity findByConnectAndNewDoctor(ConnectionEntity connect, DoctorEntity newDoctor);

    @Query("select c from ConsentEntity c where c.connect in :connectionEntities")
    List<ConsentEntity> findAllByConnect(List<ConnectionEntity> connectionEntities);

    ConsentEntity findConsentById(Integer consentId);

    @Query("select c from ConsentEntity  c where c.newDoctor = :doctorEntity and c.patientConsent = 'accepted' and c.seniorDoctorConsent = 'accepted'")
    List<ConsentEntity> findApprovedConsentForDoctor(DoctorEntity doctorEntity);
}
