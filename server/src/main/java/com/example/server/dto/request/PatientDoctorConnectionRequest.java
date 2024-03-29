package com.example.server.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PatientDoctorConnectionRequest {
    @JsonProperty("patient")
    private Patient patient;

    @JsonProperty("doctor")
    private Doctor doctor;

    @Getter
    public static class Patient{
        @JsonProperty("email")
        private String email;

        @JsonProperty("mainSymptom")
        private String mainSymptom;

        @JsonProperty("secondarySymptom")
        private String secondarySymptom;

        @JsonProperty("appointmentTimeDate")
        private LocalDateTime appointmentTimeDate;
    }

    @Getter
    public static class Doctor{
        @JsonProperty("email")
        private String email;
    }
}
