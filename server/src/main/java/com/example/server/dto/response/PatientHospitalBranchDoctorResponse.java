package com.example.server.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class PatientHospitalBranchDoctorResponse {
    private List<NameResponse> hospitallist;

    private List<NameResponse> patientList;

    private List<BranchWiseDoctorResponse> branchWiseDoctorResponseList;
}
