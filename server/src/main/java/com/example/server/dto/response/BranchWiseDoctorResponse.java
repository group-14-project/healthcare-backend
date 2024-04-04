package com.example.server.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class BranchWiseDoctorResponse {
    private String specialization;

    private List<NameResponse> nameResponses;
}
