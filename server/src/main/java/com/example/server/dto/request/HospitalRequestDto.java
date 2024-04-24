package com.example.server.dto.request;
import com.example.server.hospital.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HospitalRequestDto
{
    private String hospitalName;

    private String email;

    private String address;

    private String city;

    private String pinCode;
}