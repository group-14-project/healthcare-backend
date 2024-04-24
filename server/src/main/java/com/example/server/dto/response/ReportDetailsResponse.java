package com.example.server.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportDetailsResponse {
    private Integer id;
    private String reportName;
    private LocalDateTime localDateTime;
    private String doctorFirstName;
    private String doctorLastName;
}
