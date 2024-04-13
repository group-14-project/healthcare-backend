package com.example.server.errorOrSuccessMessageResponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ErrorMessage {
    @JsonProperty("errorMessage")
    String errorMessage;
}
