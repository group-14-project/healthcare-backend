package com.example.server.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AddPrescriptionRecordingLink {
    @JsonProperty("prescription")
    private String prescription;

    @JsonProperty("patientEmail")
    private String patientEmail;
}
