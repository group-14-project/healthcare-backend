package com.example.server.patient;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private Long phoneNumber;

    private Float height;

    private Float weight;

    private String bloodGroup;

    private String gender;

    private String address;

    private String city;

    private String pinCode;

    private boolean emailVerify;

    private String otp;

    private LocalDateTime otpGeneratedTime;
}
