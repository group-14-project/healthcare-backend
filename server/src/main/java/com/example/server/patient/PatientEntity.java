package com.example.server.patient;

import com.example.server.connection.ConnectionEntity;
import com.example.server.hospital.Role;
import com.example.server.report.ReportEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private Role role;

    @OneToMany(mappedBy = "patient")
    private List<ConnectionEntity> connection;

    @OneToMany(mappedBy = "pat")
    private List<ReportEntity> report;
}
