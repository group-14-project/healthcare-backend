package com.example.server.reviews;
import com.example.server.connection.ConnectionEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name= "review")
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String review;

    private LocalDateTime localDateTime;

    @OneToOne
    @JoinColumn(name = "connectionId")
    private ConnectionEntity conn1;
}
