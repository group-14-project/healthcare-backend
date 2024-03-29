package com.example.server.doctor;

import com.example.server.connection.ConnectionEntity;
import com.example.server.consent.ConsentEntity;
import com.example.server.hospital.Role;
import com.example.server.hospitalSpecialization.HospitalSpecializationEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class DoctorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NonNull
    private String firstName;

    @NonNull
    private String lastName;

    @NonNull
    private String email;

    private String password;

    private String registrationId;

    private String degree;

    private Long phoneNumber;

    private Integer activeStatus;

    private String otp;

    private LocalDateTime otpGeneratedTime;

    private Role role;

    @ManyToOne
    @JoinColumn(name = "hospital_specialization_id")
    private HospitalSpecializationEntity hospitalSpecialization;

    @OneToOne(mappedBy = "headDoctor")
    private HospitalSpecializationEntity hospitalSpecializationhead;

    @OneToMany(mappedBy = "doctor")
    private List<ConnectionEntity> connection;

    @OneToMany(mappedBy = "doct")
    private List<ConsentEntity> consent;
}
