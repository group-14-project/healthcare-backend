package com.example.server.consent;

import com.example.server.connection.ConnectionEntity;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.patient.PatientEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsentService {
    private final ConsentRepository consentRepo;

    private final EmailSender emailSender;

    public ConsentService(ConsentRepository consentRepo, EmailSender emailSender) {
        this.consentRepo = consentRepo;
        this.emailSender = emailSender;
    }

    public ConsentEntity saveConsent(ConsentEntity consentEntity) {
        ConsentEntity currConsent = consentRepo.findByConnectAndNewDoctor(consentEntity.getConnect(), consentEntity.getNewDoctor());
        if(currConsent!=null && !currConsent.isPatientConsent() && !currConsent.isSeniorDoctorConsent()){
            return null;
        }
        if(currConsent!=null){
            currConsent.setPatientConsent(false);
            currConsent.setLocalDate(LocalDate.now());
            currConsent.setSeniorDoctorConsent(false);
            currConsent.setFirstTimeWithdraw(false);
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

    public ConsentEntity getConsentById(Integer consentId) {
        return consentRepo.findConsentById(consentId);
    }

    public void givePatientConsent(Integer id) {
        ConsentEntity consentEntity = consentRepo.findConsentById(id);
        consentEntity.setPatientConsent(true);
        consentRepo.save(consentEntity);
    }

    public void withdrawPatientConsent(Integer id) {
        ConsentEntity consentEntity = consentRepo.findConsentById(id);
        consentEntity.setPatientConsent(false);
        consentEntity.setSeniorDoctorConsent(false);
        consentRepo.save(consentEntity);
    }

    public void patientApproved(ConsentEntity consentEntity, PatientEntity patientEntity) {
        List<String> emails = new ArrayList<>();
        emails.add(consentEntity.getConnect().getDoctor().getEmail());
        emails.add(consentEntity.getConnect().getPatient().getEmail());
        emails.add(consentEntity.getConnect().getDoctor().getHospitalSpecializationhead().getHeadDoctor().getEmail());

        emailSender.approvedPatientConsentToMainDoctor(
                emails, patientEntity.getFirstName(), consentEntity.getConnect().getDoctor().getFirstName(),
                consentEntity.getNewDoctor().getFirstName());

    }

    public void sendApprovalEmailToNewDoctor(ConsentEntity consentEntity) {
        if(consentEntity.isPatientConsent() && consentEntity.isSeniorDoctorConsent()){
            emailSender.sendApprovalEmailNewDoctor(consentEntity.getNewDoctor().getEmail(),
                    consentEntity.getConnect().getPatient().getFirstName(),
                    consentEntity.getConnect().getDoctor().getFirstName(),
                    consentEntity.getNewDoctor().getFirstName()
                    );
        }
    }

    public List<ConsentEntity> findPendingConsentBySrDoctor(List<ConsentEntity> consentEntities) {
        return consentEntities.stream()
                .filter(consent -> !consent.isSeniorDoctorConsent()).sorted(Comparator.comparing(ConsentEntity::getLocalDate)).collect(Collectors.toList());
    }

    public List<ConsentEntity> findApprovedConsentBySrDoctor(List<ConsentEntity> consentEntities) {
        return consentEntities.stream()
                .filter(ConsentEntity::isSeniorDoctorConsent).sorted(Comparator.comparing(ConsentEntity::getLocalDate)).collect(Collectors.toList());

    }

    public void giveDoctorConsent(Integer id) {
        ConsentEntity consentEntity = consentRepo.findConsentById(id);
        consentEntity.setSeniorDoctorConsent(true);
        consentRepo.save(consentEntity);
    }

    public void seniorDrApproved(ConsentEntity consentEntity, PatientEntity patientEntity) {
        List<String> emails = new ArrayList<>();
        emails.add(consentEntity.getConnect().getDoctor().getEmail());
        emails.add(consentEntity.getConnect().getPatient().getEmail());
        emails.add(consentEntity.getConnect().getDoctor().getHospitalSpecializationhead().getHeadDoctor().getEmail());

        emailSender.approvedSrDoctorConsentToMainDoctor(
                emails, patientEntity.getFirstName(), consentEntity.getConnect().getDoctor().getFirstName(),
                consentEntity.getNewDoctor().getFirstName());

    }
}
