package com.example.server.consultation;

import com.example.server.connection.ConnectionEntity;
import com.example.server.dto.response.AppointmentDetailsDto;
import com.example.server.dto.response.EachDayCount;
import com.example.server.patient.PatientController;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsultationService {
    private final ConsultationRepository consultationRepo;

    public ConsultationService(ConsultationRepository consultationRepo) {
        this.consultationRepo = consultationRepo;
    }

    public ConsultationEntity addConsultation( String symptom, String secondarySymptoms, ConnectionEntity connection, LocalDateTime dateTime){
        ConsultationEntity newConsultation = new ConsultationEntity();
        newConsultation.setConnectionId(connection);
        newConsultation.setAppointmentDateAndTime(dateTime);
        newConsultation.setMainSymptom(symptom);
        newConsultation.setSecondarySymptom(secondarySymptoms);

        return consultationRepo.save(newConsultation);
    }

    public ConsultationEntity setPrescriptionRecordingLink(String prescription, String recordingLink, Integer id) {
        ConsultationEntity consultationEntity = consultationRepo.findById(id).orElseThrow(PatientController.UnexpectedErrorException::new);
        consultationEntity.setPrescription(prescription);
        consultationEntity.setRecordingLink(recordingLink);

        return consultationRepo.save(consultationEntity);
    }

    public List<AppointmentDetailsDto> findPastAppointments(List<ConnectionEntity> connectionEntities) {
        LocalDateTime currentTime = LocalDateTime.now();
        List<ConsultationEntity>  consultationEntities = consultationRepo.findAllbyPast(connectionEntities, currentTime);
        return consultationEntities.stream()
                .map(this::mapToAppointmentDetails).collect(Collectors.toList());
    }

    public List<AppointmentDetailsDto> findFutureAppointments(List<ConnectionEntity> connectionEntities) {
        LocalDateTime currentTime = LocalDateTime.now();
        List<ConsultationEntity>  consultationEntities = consultationRepo.findAllbyFuture(connectionEntities, currentTime);
        return consultationEntities.stream()
                .map(this::mapToAppointmentDetails).collect(Collectors.toList());
    }

    private AppointmentDetailsDto mapToAppointmentDetails(ConsultationEntity consultationEntity) {
        AppointmentDetailsDto appointmentDetailsDto = new AppointmentDetailsDto();
        appointmentDetailsDto.setAppointmentDateAndTime(consultationEntity.getAppointmentDateAndTime());
        appointmentDetailsDto.setPrescription(consultationEntity.getPrescription());
        appointmentDetailsDto.setDoctorEmail(consultationEntity.getConnectionId().getDoctor().getEmail());
        appointmentDetailsDto.setMainSymptom(consultationEntity.getMainSymptom());
        appointmentDetailsDto.setPatientEmail(consultationEntity.getConnectionId().getPatient().getEmail());
        appointmentDetailsDto.setRecordingLink(consultationEntity.getRecordingLink());

        return appointmentDetailsDto;
    }


    public Integer countAppointments(List<ConnectionEntity> connectionEntities)
    {
        Integer countAppointment =consultationRepo.countAppointments(connectionEntities);
        return countAppointment;
    }

    public List<EachDayCount> sendEachDayCount(List<ConnectionEntity> connectionEntities){
        List<LocalDateTime> localDateTimes = consultationRepo.findLocalDateTimeByConnectionId(connectionEntities);
        List<LocalDate> dates = localDateTimes.stream()
                .map(LocalDateTime::toLocalDate)
                .collect(Collectors.toList());

        Map<LocalDate, Long> dateCountMap = dates.stream()
                .collect(Collectors.groupingBy(date -> date, Collectors.counting()));

        List<EachDayCount> eachDayCounts = dateCountMap.entrySet().stream()
                .map(entry -> new EachDayCount(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return eachDayCounts;

    }




}
