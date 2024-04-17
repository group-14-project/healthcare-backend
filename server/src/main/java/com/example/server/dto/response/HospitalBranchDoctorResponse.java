package com.example.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class HospitalBranchDoctorResponse {
    @JsonProperty("hospital")
    private String hospital;

    @JsonProperty("specializationNames")
    private List<SpecializationName> specializationNames;
}
