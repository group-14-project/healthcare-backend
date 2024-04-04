package com.example.server.consent;

import com.example.server.connection.ConnectionEntity;
import com.example.server.doctor.DoctorEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="consent")
public class ConsentEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        private boolean seniorDoctorConsent;
        private boolean patientConsent;

        @ManyToOne
        @JoinColumn(name = "connectionId")
        private ConnectionEntity connect;

        @ManyToOne
        @JoinColumn(name = "doctorId")
        private DoctorEntity newDoctor;
}
