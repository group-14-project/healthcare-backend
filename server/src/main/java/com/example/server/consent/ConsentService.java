package com.example.server.consent;

import com.example.server.connection.ConnectionEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsentService {
    private final ConsentRepository consentRepo;

    public ConsentService(ConsentRepository consentRepo) {
        this.consentRepo = consentRepo;
    }

    public ConsentEntity saveConsent(ConsentEntity consentEntity) {
        ConsentEntity currConsent = consentRepo.findByConnectAndNewDoctor(consentEntity.getConnect(), consentEntity.getNewDoctor());
        if(currConsent!=null){
            currConsent.setPatientConsent(false);
            currConsent.setLocalDate(LocalDate.now());
            currConsent.setSeniorDoctorConsent(false);
            return consentRepo.save(currConsent);
        }
        return consentRepo.save(consentEntity);
    }

    public List<ConsentEntity> sortConsentEntitiesByDate(List<ConsentEntity> consentEntities) {
        return consentEntities.stream()
                .sorted(Comparator.comparing(ConsentEntity::getLocalDate))
                .collect(Collectors.toList());
    }


    public List<ConsentEntity> findByConnect(List<ConnectionEntity> connectionEntities) {
        List<ConsentEntity> consentEntities = consentRepo.findAllByConnect(connectionEntities);
        return sortConsentEntitiesByDate(consentEntities);
    }
}
