package com.example.server.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddSpecializationRequest {
    @JsonProperty("specialization")
    private AddSpecializationRequest.Specialization specialization;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Specialization{
        @JsonProperty("name")
        private String name;

        @JsonProperty("hospitalId")
        private Integer hospitalId;

        @JsonProperty("doctorFirstName")
        private String doctorFirstName;

        @JsonProperty("doctorLastName")
        private String doctorLastName;

        @JsonProperty("doctorEmail")
        private String doctorEmail;

        @JsonProperty("doctorRegistrationId")
        private String doctorRegistrationId;
    }
}