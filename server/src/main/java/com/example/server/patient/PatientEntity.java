package com.example.server.patient;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NonNull
    private String firstName;

    @NonNull
    private String lastName;

    @NonNull
    private String email;

    @NonNull
    private String password;

    private Integer phoneNumber;

    private Integer height;

    private Integer weight;

    private String bloodGroup;

    private String gender;

    private String address;

    private String city;

    private String pinCode;

    private boolean emailVerify;

    private String otp;

    private LocalDateTime otpGeneratedTime;


}
