package com.example.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SeniorDoctorViewConsentResponse {
    @JsonProperty("pendingConsents")
    List<ViewConsentResponse> pendingConsents;

    @JsonProperty("approvedConsents")
    List<ViewConsentResponse> approvedConsents;
}
