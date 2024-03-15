package com.example.server.specialization;

import com.example.server.hospitalSpecialization.HospitalSpecializationEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "specialization")
public class SpecializationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NonNull
    private String name;

    private String symptoms;

    @OneToMany(mappedBy = "specialization")
    private List<HospitalSpecializationEntity> hospitalSpecializationEntities = new ArrayList<>();
}
