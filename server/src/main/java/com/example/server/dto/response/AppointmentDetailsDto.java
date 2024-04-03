package com.example.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentDetailsDto {
    @JsonProperty("patientEmail")
    private String patientEmail;

    @JsonProperty("doctorEmail")
    private String doctorEmail;

    @JsonProperty("mainSymptom")
    private String mainSymptom;

    @JsonProperty("prescription")
    private String prescription;

    @JsonProperty("recordingLink")
    private String recordingLink;

    @JsonProperty("appointmentDateAndTime")
    private LocalDateTime appointmentDateAndTime;

}
