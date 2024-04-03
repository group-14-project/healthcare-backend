package com.example.server.converter;

import com.example.server.dto.response.PatientResponse;
import com.example.server.jwtToken.JWTService;
import com.example.server.patient.PatientEntity;
import org.springframework.stereotype.Service;

@Service
public class PatientObjectConverter {
//    private final JWTService jwtService;
//
//    public PatientObjectConverter(JWTService jwtService){
//        this.jwtService = jwtService;
//    }
//
//    public PatientResponse entityToResponse(PatientEntity patientEntity){
//        return PatientResponse.fromPatientEntity(patientEntity, jwtService.createJwt(patientEntity.getEmail(), "patient"));
//    }
}
