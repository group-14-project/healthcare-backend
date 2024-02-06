package com.example.server.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class SignupPatientRequest {
    @JsonProperty("patient")
    private Patient patient;

    @Getter
    public static class Patient{

        @JsonProperty("password")
        private String password;

        @JsonProperty("email")
        private String email;

        @JsonProperty("name")
        private String name;
    }
}
