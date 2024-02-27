package com.example.server.dto.response;

import com.example.server.patient.PatientEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PatientResponse {
    @JsonProperty("patient")
    private final Patient patient;

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Patient{
        @JsonProperty("email")
        private final String email;

        @JsonProperty("token")
        private final String token;

        @JsonProperty("name")
        private String name;
    }

    public static PatientResponse fromPatientEntity(PatientEntity patientEntity, String token){
        return new PatientResponse(
                new Patient(
                        patientEntity.getEmail(),
                        token,
                        patientEntity.getFirstName()
                )
        );
    }
}
