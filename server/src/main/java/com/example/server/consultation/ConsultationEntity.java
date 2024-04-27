package com.example.server.consultation;

import com.example.server.connection.ConnectionEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="consultation")
public class ConsultationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String mainSymptom;

    private String secondarySymptom;

    private String prescription;

    private String recordingLink;

    private LocalDateTime appointmentDateAndTime;

    @ManyToOne
    @JoinColumn(name = "connectionId")
    private ConnectionEntity connectionId;
}
