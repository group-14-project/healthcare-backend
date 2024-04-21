package com.example.server.consultation;

import com.example.server.dto.request.AddPrescriptionRecordingLink;
import com.example.server.patient.PatientController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doctor")
@CrossOrigin
public class ConsultationController {
    private final ConsultationService consultation;

    public ConsultationController(ConsultationService consultation) {
        this.consultation = consultation;
    }

    //TODO Shift to doctor controller
    @PostMapping("/addPrescriptionRecordLink/{id}")
    public ResponseEntity<Void> addPrescriptionRecordLink(@RequestBody AddPrescriptionRecordingLink addPrescriptionRecordingLink, @PathVariable Integer id){
        ConsultationEntity consultationEntity = consultation.setPrescriptionRecordingLink(addPrescriptionRecordingLink.getPrescription(),
                addPrescriptionRecordingLink.getRecordingLink(), id);
        if(consultationEntity==null){
            throw new PatientController.UnexpectedErrorException();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
