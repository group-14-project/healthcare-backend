package com.example.server.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ReportUploadRequest {
    private MultipartFile file;
    private String doctorEmail;
    private String reportName;
}
