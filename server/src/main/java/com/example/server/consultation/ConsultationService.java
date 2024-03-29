package com.example.server.consultation;

import com.example.server.connection.ConnectionEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

}
