package com.example.server.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GiveConsentRequest {
    @JsonProperty("consentId")
    private Integer consentId;
}
