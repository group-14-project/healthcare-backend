package com.example.server.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AddReviewRequest {
    @JsonProperty("doctorEmail")
    private String doctorEmail;

    @JsonProperty("patientEmail")
    private String patientEmail;

    @JsonProperty("review")
    private String review;

    @JsonProperty("rating")
    private Integer rating;
}
