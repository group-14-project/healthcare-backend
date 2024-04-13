package com.example.server.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class PatientHospitalBranchDoctorResponse {
    private List<DoctorDetailsResponse> doctorsList;

    private List<NameResponse> patientList;
}
