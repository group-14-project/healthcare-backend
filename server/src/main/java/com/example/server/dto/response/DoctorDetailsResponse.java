package com.example.server.dto.response;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

//TODO: send details of Doctor Patient for the consent
@Data
public class DoctorDetailsResponse {
    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("hospitalName")
    private String hospitalName;

    @JsonProperty("specialization")
    private String specialization;

    @JsonProperty("doctorEmail")
    private String doctorEmail;

    @JsonProperty("degree")
    private String degree;

    @JsonProperty("imageUrl")
    private String imageUrl;
}
