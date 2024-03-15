package com.example.server.hospitalSpecialization;

import com.example.server.doctor.DoctorEntity;
import com.example.server.hospital.HospitalEntity;
import com.example.server.specialization.SpecializationEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hospital_specialization")
public class HospitalSpecializationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "specialization_id")
    private SpecializationEntity specialization;

    @ManyToOne
    @JoinColumn(name = "hospital_id")
    private HospitalEntity hospital;

    @OneToOne
    @JoinColumn(name = "head_doctor_id")
    private DoctorEntity headDoctor;

    @OneToMany(mappedBy = "hospitalSpecialization")
    private List<DoctorEntity> doctors;
}
