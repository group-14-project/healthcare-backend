package com.example.server.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ViewReviewsResponse {
    private String doctorFirstName;
    private String doctorLastName;
    private String review;
    private String patientFirstName;
    private String patientLastName;
    private LocalDateTime dateTime;
}
