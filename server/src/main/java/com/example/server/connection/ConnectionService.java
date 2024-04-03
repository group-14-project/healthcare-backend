package com.example.server.connection;

import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorService;
import com.example.server.patient.PatientController;
import com.example.server.patient.PatientEntity;
import com.example.server.patient.PatientService;
import org.springframework.stereotype.Service;

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
        return connectionRepo.save(newConnection);
    }

    public ConnectionEntity findConnection(String doctorEmail, String patientEmail){
        DoctorEntity currDoctor = doctor.doctorDetails(doctorEmail);
        PatientEntity currPatient = patient.patientDetails(patientEmail);
        if(currPatient==null || currDoctor==null){
            throw new PatientController.UnexpectedErrorException();
        }
        ConnectionEntity connectionEntity = connectionRepo.findByDoctorAndPatient(currDoctor, currPatient);
        if(connectionEntity==null){
            throw new PatientController.UnexpectedErrorException();
        }
        return connectionEntity;
    }
}