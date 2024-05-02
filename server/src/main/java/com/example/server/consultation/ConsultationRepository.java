package com.example.server.consultation;

import com.example.server.connection.ConnectionEntity;
import com.example.server.dto.response.EachDayCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;



@Repository
public interface ConsultationRepository extends JpaRepository<ConsultationEntity, Integer> {

    @Query("SELECT c FROM ConsultationEntity c WHERE c.appointmentDateAndTime < :currentTime AND c.connectionId IN :connectionEntities")
    List<ConsultationEntity> findAllbyPast(List<ConnectionEntity> connectionEntities, LocalDateTime currentTime);

    @Query("SELECT c FROM ConsultationEntity c WHERE c.appointmentDateAndTime >= :currentTime AND c.connectionId IN :connectionEntities")
    List<ConsultationEntity> findAllbyFuture(List<ConnectionEntity> connectionEntities, LocalDateTime currentTime);

    @Query("Select COUNT(c) FROM ConsultationEntity c WHERE c.connectionId IN :connectionEntities")
    Integer countAppointments(List<ConnectionEntity> connectionEntities);

    @Query("select c.appointmentDateAndTime from ConsultationEntity c WHERE c.connectionId IN :connectionEntities")
    List<LocalDateTime> findLocalDateTimeByConnectionId(List<ConnectionEntity> connectionEntities);

    @Query("SELECT c FROM ConsultationEntity c WHERE c.appointmentDateAndTime " +
            "BETWEEN :startTime AND :endTime")

    List<ConsultationEntity> getByAppointmentDateAndTime(LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT c FROM ConsultationEntity c WHERE c.appointmentDateAndTime < :currentTime " +
            "AND c.appointmentDateAndTime = (" +
            "SELECT MAX(c2.appointmentDateAndTime) FROM ConsultationEntity c2 " +
            "WHERE c2.connectionId IN :connectionEntities " +
            "AND c2.connectionId = c.connectionId)")
    List<ConsultationEntity> findAllLatestByDoctor(List<ConnectionEntity> connectionEntities, LocalDateTime currentTime);


    @Query("SELECT c FROM ConsultationEntity c WHERE c.connectionId = :connection AND c.appointmentDateAndTime < :now ORDER BY c.appointmentDateAndTime DESC LIMIT 1")
    ConsultationEntity findLatestByConnection(ConnectionEntity connection, LocalDateTime now);
}
