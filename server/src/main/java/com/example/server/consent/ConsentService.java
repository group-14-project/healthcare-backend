package com.example.server.consent;

import com.example.server.connection.ConnectionEntity;
import com.example.server.doctor.DoctorEntity;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.patient.PatientEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ConsentService {
    private final ConsentRepository consentRepo;

    private final EmailSender emailSender;

    public ConsentService(ConsentRepository consentRepo, EmailSender emailSender) {
        this.consentRepo = consentRepo;
        this.emailSender = emailSender;
    }

    public ConsentEntity saveNewConsent(ConsentEntity consentEntity) {
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
        consentEntity.setPatientConsent("accepted");
        consentRepo.save(consentEntity);
    }

    public void withdrawPatientConsent(Integer id) {
        ConsentEntity consentEntity = consentRepo.findConsentById(id);
        consentEntity.setPatientConsent("withdrawn");
        consentRepo.save(consentEntity);
    }

    public void patientApproved(ConsentEntity consentEntity, PatientEntity patientEntity) {
        List<String> emails = new ArrayList<>();
        emails.add(consentEntity.getConnect().getDoctor().getEmail());
        emails.add(consentEntity.getConnect().getPatient().getEmail());
        emails.add(consentEntity.getConnect().getDoctor().getHospitalSpecialization().getHeadDoctor().getEmail());

        emailSender.approvedPatientConsentToMainDoctor(
                emails, patientEntity.getFirstName(), consentEntity.getConnect().getDoctor().getFirstName(),
                consentEntity.getNewDoctor().getFirstName());

    }

    public void sendApprovalEmailToNewDoctor(ConsentEntity consentEntity) {
        if(Objects.equals(consentEntity.getPatientConsent(), "accepted") && Objects.equals(consentEntity.getSeniorDoctorConsent(), "accepted")){
            emailSender.sendApprovalEmailNewDoctor(consentEntity.getNewDoctor().getEmail(),
                    consentEntity.getConnect().getPatient().getFirstName(),
                    consentEntity.getConnect().getDoctor().getFirstName(),
                    consentEntity.getNewDoctor().getFirstName()
                    );
        }
    }

    public List<ConsentEntity> findPendingConsentBySrDoctor(List<ConsentEntity> consentEntities) {
        return consentEntities.stream()
                .filter(consent -> Objects.equals(consent.getSeniorDoctorConsent(), "pending")).sorted(Comparator.comparing(ConsentEntity::getLocalDate)).collect(Collectors.toList());
    }

    public List<ConsentEntity> findApprovedConsentBySrDoctor(List<ConsentEntity> consentEntities) {
        return consentEntities.stream()
                .filter(consent -> Objects.equals(consent.getSeniorDoctorConsent(), "accepted")).sorted(Comparator.comparing(ConsentEntity::getLocalDate)).collect(Collectors.toList());
    }

    public void giveDoctorConsent(Integer id) {
        ConsentEntity consentEntity = consentRepo.findConsentById(id);
        consentEntity.setSeniorDoctorConsent("accepted");
        consentRepo.save(consentEntity);
    }

    public void seniorDrApproved(ConsentEntity consentEntity, PatientEntity patientEntity) {
        List<String> emails = new ArrayList<>();
        emails.add(consentEntity.getConnect().getDoctor().getEmail());
        emails.add(consentEntity.getConnect().getPatient().getEmail());
        emails.add(consentEntity.getConnect().getDoctor().getHospitalSpecialization().getHeadDoctor().getEmail());

        emailSender.approvedSrDoctorConsentToMainDoctor(
                emails, patientEntity.getFirstName(), consentEntity.getConnect().getDoctor().getFirstName(),
                consentEntity.getNewDoctor().getFirstName());

    }

    public ConsentEntity getConsentByConnectionAndSeniorDoctor(ConnectionEntity connectionEntity, DoctorEntity newDoctorEntity) {
        return consentRepo.findByConnectAndNewDoctor(connectionEntity, newDoctorEntity);
    }

    public void rejectConsent(Integer id) {
        ConsentEntity consentEntity = consentRepo.findConsentById(id);
        consentEntity.setPatientConsent("rejected");
        consentEntity.setSeniorDoctorConsent("rejected");
        consentRepo.save(consentEntity);
    }

    public void sendRejectionEmailToNewDoctor(ConsentEntity consentEntity) {
        emailSender.sendRejectionEmailToDoctor(consentEntity.getConnect().getDoctor().getEmail(), consentEntity.getNewDoctor().getFirstName(), consentEntity.getConnect().getPatient().getFirstName());
    }

    public List<ConsentEntity> findApprovedConsentByDoctorAndPatientByNewDoctor(DoctorEntity doctorEntity) {
        return consentRepo.findApprovedConsentForDoctor(doctorEntity);
    }

    public ConnectionEntity findByNewDoctorAndID(DoctorEntity doctorEntity, Integer id) {
        ConsentEntity consentEntity = consentRepo.findConsentById(id);
        if(consentEntity==null || consentEntity.getNewDoctor()!=doctorEntity || !Objects.equals(consentEntity.getPatientConsent(), "accepted") || !Objects.equals(consentEntity.getSeniorDoctorConsent(), "accepted")){
            return null;
        }
        return consentEntity.getConnect();
    }

}
