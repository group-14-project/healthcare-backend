package com.example.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class PatientUpdateDetails {

    @JsonProperty("email")
    private  String email;

    @JsonProperty("firstName")
    private  String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("height")
    private String height;

    @JsonProperty("weight")
    private String weight;

    @JsonProperty("bloodGroup")
    private String bloodGroup;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("firstTimeLogin")
    private boolean firstTimeLogin;

    @JsonProperty("pastAppointments")
    private List<AppointmentDetailsDto> pastAppointmentDetails;

    @JsonProperty("futureAppointments")
    private List<AppointmentDetailsDto> futureAppointmentDetails;


}
