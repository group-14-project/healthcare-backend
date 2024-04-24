package com.example.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.List;

@Data
public class DepartmentDto {
    @JsonProperty("seniorDoctor")
    private final DoctorDetailsResponse seniorDoctor;

    @JsonProperty("specialization")
    private final String specialization;

    @JsonProperty("doctors")
    private final List<DoctorDetailsResponse> doctors;
}
