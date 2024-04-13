package com.example.server.hospital;

import com.example.server.hospitalSpecialization.HospitalSpecializationEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hospital")
public class HospitalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String hospitalName;
    private String email;
    private String password;
    private String address;
    private String city;
    private String pinCode;
    private String otp;
    private boolean firstTimeLogin;
    private LocalDateTime otpGeneratedTime;
    private boolean emailVerify;
    private String role;
    private LocalDateTime lastAccessTime;
    private String jwtToken;
    @OneToMany(mappedBy = "hospital")
    private List<HospitalSpecializationEntity> hospitalSpecializationEntities = new ArrayList<>();
}
