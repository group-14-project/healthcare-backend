package com.example.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentDetailsDto {
    @JsonProperty("patientEmail")
    private String patientEmail;

    @JsonProperty("patientFirstName")
    private String patientFirstName;

    @JsonProperty("patientLastName")
    private String patientLastName;

    @JsonProperty("doctorEmail")
    private String doctorEmail;

    @JsonProperty("doctorFirstName")
    private String doctorFirstName;

    @JsonProperty("doctorLastName")
    private String doctorLastName;

    @JsonProperty("mainSymptom")
    private String mainSymptom;

    @JsonProperty("prescription")
    private String prescription;

    @JsonProperty("recordingLink")
    private String recordingLink;

    @JsonProperty("appointmentDateAndTime")
    private LocalDateTime appointmentDateAndTime;

}
