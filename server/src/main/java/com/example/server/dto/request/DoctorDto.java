package com.example.server.dto.request;
import lombok.Data;
@Data
public class DoctorDto
{
    private String hospitalEmail;
    private String specialization;
    private String doctorEmail;
    private String firstName;
    private String lastName;
    private String registrationId;
}

