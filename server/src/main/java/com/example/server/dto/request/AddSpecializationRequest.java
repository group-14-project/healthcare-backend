package com.example.server.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddSpecializationRequest {
        @JsonProperty("specializationName")
        private String specializationName;

        @JsonProperty("doctorFirstName")
        private String doctorFirstName;

        @JsonProperty("doctorLastName")
        private String doctorLastName;

        @JsonProperty("doctorEmail")
        private String doctorEmail;

        @JsonProperty("doctorRegistrationId")
        private String doctorRegistrationId;

        @JsonProperty("degree")
        private String degree;

        @JsonProperty("phoneNumber")
        private String phoneNumber;
}
