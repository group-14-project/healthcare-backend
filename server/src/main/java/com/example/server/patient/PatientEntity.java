package com.example.server.patient;

import com.example.server.connection.ConnectionEntity;
import com.example.server.hospital.Role;
import com.example.server.report.ReportEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    private String phoneNumber;

    private String height;

    private String weight;

    private String bloodGroup;

    private String gender;

    private String address;

    private String city;

    private String pinCode;

    private boolean emailVerify;

    private boolean firstTimeLogin;

    private String otp;

    private LocalDateTime otpGeneratedTime;

    private String role;

    private String jwtToken;

    private LocalDateTime lastAccessTime;

    private boolean deleteEntry;

    private LocalDate deletionTime;

    @OneToMany(mappedBy = "patient")
    private List<ConnectionEntity> connection;

    @OneToMany(mappedBy = "pat")
    private List<ReportEntity> report;

}
