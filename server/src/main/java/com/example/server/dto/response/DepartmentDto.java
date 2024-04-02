package com.example.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.List;

@Getter
@Setter
public class DepartmentDto {
    @JsonProperty("seniorDoctor")
    private final String seniorDoctor;

    @JsonProperty("specialization")
    private final String specialization;

    @JsonProperty("doctors")
    private final List<String> doctors;

    public DepartmentDto(String seniorDoctor, String specialization, List<String> doctors) {
        this.doctors = doctors;
        this.seniorDoctor =seniorDoctor;
        this.specialization = specialization;
    }
}
