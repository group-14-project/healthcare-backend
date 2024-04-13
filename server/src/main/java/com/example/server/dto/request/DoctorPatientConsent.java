package com.example.server.dto.request;

import lombok.Data;

@Data
public class DoctorPatientConsent {
    private String patientEmail;
    private String newDoctorEmail;
}
