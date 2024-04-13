package com.example.server.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PasswordUpdateRequest {
    @JsonProperty("password")
    private String password;
}
