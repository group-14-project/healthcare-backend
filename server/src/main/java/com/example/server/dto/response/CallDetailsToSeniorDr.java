package com.example.server.dto.response;

import lombok.Data;

@Data
public class CallDetailsToSeniorDr {
    private Integer doctorId;
    private Integer patientId;
    private String patientName;
    private String doctorName;
}
