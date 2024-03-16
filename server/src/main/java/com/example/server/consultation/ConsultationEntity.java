package com.example.server.consultation;

import com.example.server.connection.ConnectionEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="consultation")
public class ConsultationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String mainSymptom;
    private String secSymptom;
    private String prescription;
    @Lob
    private byte[] recordingLink;
    @Temporal(TemporalType.TIMESTAMP)
    private Date myDate;
    @ManyToOne
    @JoinColumn(name = "connectionId")
    private ConnectionEntity conn;
}
