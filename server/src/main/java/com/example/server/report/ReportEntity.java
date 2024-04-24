package com.example.server.report;

import com.example.server.connection.ConnectionEntity;
import com.example.server.patient.PatientEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.generate.GeneratedTypeReference;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name= "report")
public class ReportEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String reportName;

  private String fileName;

  private LocalDateTime dateTime;

  @ManyToOne
  @JoinColumn(name = "patientId")
  private PatientEntity pat;

  @ManyToOne
  @JoinColumn(name = "connectionId")
  private ConnectionEntity con;
 }
