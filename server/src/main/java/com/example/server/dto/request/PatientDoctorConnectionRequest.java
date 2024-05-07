package com.example.server.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PatientDoctorConnectionRequest {

    @JsonProperty("email")
    private String email;

    @JsonProperty("mainSymptom")
    private String mainSymptom;

    @JsonProperty("secondarySymptom")
    private String secondarySymptom;
}
