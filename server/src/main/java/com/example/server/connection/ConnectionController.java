package com.example.server.connection;

import com.example.server.consultation.ConsultationEntity;
import com.example.server.consultation.ConsultationService;
import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorService;
import com.example.server.dto.request.PatientDoctorConnectionRequest;
import com.example.server.patient.PatientController;
import com.example.server.patient.PatientEntity;
import com.example.server.patient.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/connection")
public class ConnectionController {
    private final ConnectionService connection;

    private final PatientService patient;

    private final DoctorService doctor;

    private final ConsultationService consultation;

    public ConnectionController(ConnectionService connection, PatientService patient, DoctorService doctor, ConsultationService consultation) {
        this.connection = connection;
        this.patient = patient;
        this.doctor = doctor;
        this.consultation = consultation;
    }

    @PostMapping("/makeConnection")
    public ResponseEntity<Void> makeConnection(@RequestBody PatientDoctorConnectionRequest patientDoctorConnection){
        PatientEntity patientEntity = patient.patientDetails(patientDoctorConnection.getPatient().getEmail());
        DoctorEntity doctorEntity = doctor.doctorDetails(patientDoctorConnection.getDoctor().getEmail());

        if(patientEntity==null || doctorEntity==null){
            throw new PatientController.UnexpectedErrorException();
        }
        ConnectionEntity connectionEntity = connection.findOrMakeConnection(patientEntity, doctorEntity);
        if(connectionEntity==null){
            throw new PatientController.UnexpectedErrorException();
        }

        ConsultationEntity consultationEntity = consultation.addConsultation(
                patientDoctorConnection.getPatient().getMainSymptom(),
                patientDoctorConnection.getPatient().getSecondarySymptom(),
                connectionEntity,
                patientDoctorConnection.getPatient().getAppointmentTimeDate()
                );
        if(consultationEntity==null){
            throw new PatientController.UnexpectedErrorException();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
