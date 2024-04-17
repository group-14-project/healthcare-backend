package com.example.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SpecializationName {
    @JsonProperty("specialization")
    private String specialization;

    @JsonProperty("doctors")
    private List<NameResponse> doctors;
}
