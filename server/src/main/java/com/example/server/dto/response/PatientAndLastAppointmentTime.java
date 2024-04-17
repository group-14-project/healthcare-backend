package com.example.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PatientAndLastAppointmentTime {
    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("date")
    private LocalDateTime date;

    @JsonProperty("email")
    private String email;

}
