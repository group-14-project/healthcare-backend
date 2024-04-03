package com.example.server.dto.request;
import lombok.Data;

@Data
public class PatientDetailsRequest
{
    private String email;

    private String firstName;

    private String lastName;

    private Long phoneNumber;

    private String height;

    private String weight;

    private String bloodGroup;

    private String gender;

    private String address;

    private String city;

    private String pinCode;
}
