package com.example.server.consent;

import com.example.server.connection.ConnectionEntity;
import com.example.server.doctor.DoctorEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="consent")
public class ConsentEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;
        private String seniorDoctorConsent;
        private String patientConsent;
        private LocalDate localDate;

        @ManyToOne
        @JoinColumn(name = "connectionId")
        private ConnectionEntity connect;

        @ManyToOne
        @JoinColumn(name = "doctorId")
        private DoctorEntity newDoctor;
}
