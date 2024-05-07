package com.example.server.connection;

import com.example.server.consultation.ConsultationEntity;
import com.example.server.consultation.ConsultationService;
import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorService;
import com.example.server.dto.request.PatientDoctorConnectionRequest;
import com.example.server.dto.response.AppointmentDetailsDto;
import com.example.server.errorOrSuccessMessageResponse.ErrorMessage;
import com.example.server.hospital.HospitalEntity;
import com.example.server.jwtToken.JWTService;
import com.example.server.jwtToken.JWTTokenReCheck;
import com.example.server.patient.PatientController;
import com.example.server.patient.PatientEntity;
import com.example.server.patient.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.print.Doc;

@RestController
@CrossOrigin
@RequestMapping("/doctor")
public class ConnectionController {
    private final JWTService jwtService;
    private final JWTTokenReCheck jwtTokenReCheck;
    private final ConnectionService connection;
    private final PatientService patient;
    private final DoctorService doctor;
    private final ConsultationService consultation;

    public ConnectionController(JWTService jwtService, JWTTokenReCheck jwtTokenReCheck, ConnectionService connection, PatientService patient, DoctorService doctor, ConsultationService consultation) {
        this.jwtService = jwtService;
        this.jwtTokenReCheck = jwtTokenReCheck;
        this.connection = connection;
        this.patient = patient;
        this.doctor = doctor;
        this.consultation = consultation;
    }

    //JWT done
    @PostMapping("/makeConnection")
    public ResponseEntity<?> makeConnection(@RequestBody PatientDoctorConnectionRequest patientDoctorConnection, HttpServletRequest request){
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionDoctor(request);
        if(doctorEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        PatientEntity patientEntity = patient.patientDetails(patientDoctorConnection.getEmail());

        if(patientEntity == null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Unexpected Error Occurred");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        ConnectionEntity connectionEntity = connection.findOrMakeConnection(patientEntity, doctorEntity);
        if(connectionEntity==null){
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Unexpected Error Occurred");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        ConsultationEntity consultationEntity = consultation.addConsultation(
                patientDoctorConnection.getMainSymptom(),
                patientDoctorConnection.getSecondarySymptom(),
                connectionEntity
                );
        if(consultationEntity==null){
            throw new PatientController.UnexpectedErrorException();
        }
        AppointmentDetailsDto response = new AppointmentDetailsDto();
        response.setRecordingLink(consultationEntity.getRecordingLink());
        response.setMainSymptom(consultationEntity.getMainSymptom());
        response.setPatientEmail(patientEntity.getEmail());
        response.setPrescription(consultationEntity.getPrescription());
        response.setDoctorEmail(doctorEntity.getEmail());
        response.setAppointmentDateAndTime(consultationEntity.getAppointmentDateAndTime());
        response.setPatientFirstName(patientEntity.getFirstName());
        response.setPatientLastName(patientEntity.getLastName());
        response.setDoctorFirstName(doctorEntity.getFirstName());
        response.setDoctorLastName(doctorEntity.getLastName());
        return ResponseEntity.ok().body(response);
    }
}
