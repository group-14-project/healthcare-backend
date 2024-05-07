package com.example.server.connection;

import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorService;
import com.example.server.dto.response.CallDetailsToSeniorDr;
import com.example.server.hospital.HospitalEntity;
import com.example.server.hospitalSpecialization.HospitalSpecializationEntity;
import com.example.server.patient.PatientController;
import com.example.server.patient.PatientEntity;
import com.example.server.patient.PatientService;
import lombok.Data;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConnectionService {
    private final ConnectionRepository connectionRepo;

    private final DoctorService doctor;

    private final PatientService patient;

    public ConnectionService(ConnectionRepository connectionRepo, DoctorService doctor, PatientService patient) {
        this.connectionRepo = connectionRepo;
        this.doctor = doctor;
        this.patient = patient;
    }

    public ConnectionEntity findOrMakeConnection(PatientEntity patient, DoctorEntity doctor){
        ConnectionEntity connectionEntity = connectionRepo.findByDoctorAndPatient(doctor, patient);
        if(connectionEntity!=null){
            return connectionEntity;
        }
        ConnectionEntity newConnection = new ConnectionEntity();
        newConnection.setDoctor(doctor);
        newConnection.setPatient(patient);
        newConnection.setCreatedAt(LocalDate.now());
        return connectionRepo.save(newConnection);
    }

    public ConnectionEntity findConnection(String doctorEmail, String patientEmail){
        DoctorEntity currDoctor = doctor.doctorDetails(doctorEmail);
        PatientEntity currPatient = patient.patientDetails(patientEmail);
        if(currPatient==null || currDoctor==null){
            return null;
        }
        ConnectionEntity connectionEntity = connectionRepo.findByDoctorAndPatient(currDoctor, currPatient);
        if(connectionEntity==null){
            return null;
        }
        return connectionEntity;
    }

    public List<ConnectionEntity> findAllConnections(PatientEntity newPatient) {
        return connectionRepo.findByPatient(newPatient);
    }

    public List<ConnectionEntity> findAllConnectionsByDoctor(DoctorEntity newDoctor)
    {
        return connectionRepo.findByDoctor(newDoctor);
    }

    public List<PatientEntity> findAllPatientsByDoctor(DoctorEntity newDoctor){
        return connectionRepo.findPatientsByDoctor(newDoctor);
    }
    public Integer countPatient(DoctorEntity newDoctor)
    {
        return connectionRepo.countByDoctor(newDoctor);
    }

    public List<ConnectionEntity> findAllConnectionsByDoctorList(List<DoctorEntity> doctorEntities) {
        return connectionRepo.findAllByDoctorList(doctorEntities);
    }

    public List<ConnectionEntity> findAllConnectionByHospital(HospitalEntity newHospital) {
        List<ConnectionEntity> connectionEntities = connectionRepo.findAll();
        return connectionEntities.stream()
                .filter(connection -> connection.getDoctor().getHospitalSpecialization().getHospital().getHospitalName().equals(newHospital.getHospitalName()))
                .collect(Collectors.toList());
    }

    public List<ConnectionEntity> findPatientConnection(PatientEntity patientEntity)
    {
        List<ConnectionEntity> connectionEntities=connectionRepo.findByPatient(patientEntity);
        return connectionEntities;
    }
}
