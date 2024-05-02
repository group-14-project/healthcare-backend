package com.example.server.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RecommendedPatients {
    private String patientLastName;
    private String patientFirstName;
    private String doctorLastName;
    private String doctorFirstName;
    private Integer consentId;
    private LocalDate localDate;
}
