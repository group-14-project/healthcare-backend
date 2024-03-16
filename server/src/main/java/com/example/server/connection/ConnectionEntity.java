package com.example.server.connection;
import com.example.server.consent.ConsentEntity;
import com.example.server.consultation.ConsultationEntity;
import com.example.server.doctor.DoctorEntity;
import com.example.server.patient.PatientEntity;
import com.example.server.report.ReportEntity;
import com.example.server.reviews.ReviewEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="connection")
public class ConnectionEntity
{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id")
    private  int id;
    @ManyToOne
    @JoinColumn(name = "doctorId")
    private DoctorEntity doctor;

    @ManyToOne
    @JoinColumn(name = "patientId")
    private PatientEntity patient;

    @OneToMany(mappedBy = "connect")
    private List<ConsentEntity> consent;
    @OneToMany(mappedBy = "con")
    private List<ReportEntity> report;
    @OneToMany(mappedBy = "conn")
    private List<ConsultationEntity> consult;
    @OneToMany(mappedBy = "conn1")
    private List<ReviewEntity> review;
}
