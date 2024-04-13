package com.example.server.errorOrSuccessMessageResponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SuccessMessage {
    @JsonProperty("successMessage")
    private String successMessage;
}
