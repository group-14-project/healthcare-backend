package com.example.server.consultation;

import com.example.server.connection.ConnectionEntity;
import com.example.server.dto.response.AppointmentDetailsDto;
import com.example.server.dto.response.EachDayCount;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.patient.PatientController;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsultationService {
    private final ConsultationRepository consultationRepo;
    private final EmailSender emailSender;
    public ConsultationService(ConsultationRepository consultationRepo, EmailSender emailSender) {
        this.consultationRepo = consultationRepo;
        this.emailSender = emailSender;
    }

    public ConsultationEntity addConsultation( String symptom, String secondarySymptoms, ConnectionEntity connection){
        ConsultationEntity newConsultation = new ConsultationEntity();
        newConsultation.setConnectionId(connection);
        newConsultation.setAppointmentDateAndTime(LocalDateTime.now());
        newConsultation.setMainSymptom(symptom);
        newConsultation.setSecondarySymptom(secondarySymptoms);
        return consultationRepo.save(newConsultation);
    }

    public ConsultationEntity setPrescription(String prescription, ConnectionEntity connection) {
        ConsultationEntity consultationEntity = consultationRepo.findLatestByConnection(connection, LocalDateTime.now());
        consultationEntity.setPrescription(prescription);
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
        appointmentDetailsDto.setDoctorFirstName(consultationEntity.getConnectionId().getDoctor().getFirstName());
        appointmentDetailsDto.setDoctorLastName(consultationEntity.getConnectionId().getDoctor().getLastName());
        appointmentDetailsDto.setDoctorEmail(consultationEntity.getConnectionId().getDoctor().getEmail());
        appointmentDetailsDto.setMainSymptom(consultationEntity.getMainSymptom());
        appointmentDetailsDto.setPatientEmail(consultationEntity.getConnectionId().getPatient().getEmail());
        appointmentDetailsDto.setPatientFirstName(consultationEntity.getConnectionId().getPatient().getFirstName());
        appointmentDetailsDto.setPatientLastName(consultationEntity.getConnectionId().getPatient().getLastName());
        appointmentDetailsDto.setRecordingLink(consultationEntity.getRecordingLink());
        return appointmentDetailsDto;
    }


    public Integer countAppointments(List<ConnectionEntity> connectionEntities)
    {
        return consultationRepo.countAppointments(connectionEntities);
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

        Collections.sort(eachDayCounts, Comparator.comparing(EachDayCount::getDate));

        return eachDayCounts;

    }

    //TODO: make the timing every 1 hr
//    @Scheduled(fixedRate = 60000)
    public void sendAppointmentReminders() {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime reminderTime = currentTime.plusMinutes(46);
        List<ConsultationEntity> consultations = consultationRepo.getByAppointmentDateAndTime(currentTime, reminderTime);
        for (ConsultationEntity consultation : consultations) {
            emailSender.sendReminderEmail(consultation.getConnectionId().getPatient().getEmail(),
                    consultation.getConnectionId().getPatient().getFirstName(), consultation.getConnectionId().getDoctor().getFirstName(), consultation.getAppointmentDateAndTime());
            emailSender.sendReminderEmail(consultation.getConnectionId().getDoctor().getEmail(),
                    consultation.getConnectionId().getDoctor().getFirstName(), consultation.getConnectionId().getPatient().getFirstName(), consultation.getAppointmentDateAndTime());
        }
    }


    public List<ConsultationEntity> findLatestAppointment(List<ConnectionEntity> connectionEntities) {
        LocalDateTime currentTime = LocalDateTime.now();
        List<ConsultationEntity>  consultationEntities =consultationRepo.findAllLatestByDoctor(connectionEntities, currentTime);
        consultationEntities.sort((c1, c2) -> c2.getAppointmentDateAndTime().compareTo(c1.getAppointmentDateAndTime()));
        return consultationEntities;
    }
}
