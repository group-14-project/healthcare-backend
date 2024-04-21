package com.example.server.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AddReviewRequest {
    @JsonProperty("doctorEmail")
    private String doctorEmail;

    @JsonProperty("review")
    private String review;
}
