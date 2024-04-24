package com.example.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ViewConsentResponse {
    @JsonProperty("consentId")
    private Integer consentId;

    @JsonProperty("patientFirstName")
    private String patientFirstName;

    @JsonProperty("patientLastName")
    private String patientLastName;

    @JsonProperty("mainDoctorFirstName")
    private String mainDoctorFirstName;

    @JsonProperty("mainDoctorLastName")
    private String mainDoctorLastName;

    @JsonProperty("seniorDoctorFirstName")
    private String seniorDoctorFirstName;

    @JsonProperty("seniorDoctorLastName")
    private String seniorDoctorLastName;

    @JsonProperty("newDoctorFirstName")
    private String newDoctorFirstName;

    @JsonProperty("newDoctorLastName")
    private String newDoctorLastName;

    @JsonProperty("newDoctorHospitalName")
    private String newDoctorHospitalName;

    @JsonProperty("patientConsent")
    private String patientConsent;

    @JsonProperty("seniorDrConsent")
    private String seniorDrConsent;

    @JsonProperty("date")
    private LocalDate date;
}
