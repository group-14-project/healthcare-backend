package com.example.server.specialization;

import com.example.server.dto.request.MainSpecializationAddRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/specialization")
@CrossOrigin
public class SpecializationController {
    private final SpecializationService specializationService;

    public SpecializationController(SpecializationService specializationService) {
        this.specializationService = specializationService;
    }


    @PostMapping("/add")
    public ResponseEntity<Void> addSpecialization(@RequestBody MainSpecializationAddRequest body){
        SpecializationEntity specializationEntity = specializationService.addSpecialization(
                body.getMainSpecialization().getName(),
                body.getMainSpecialization().getSymptoms()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
