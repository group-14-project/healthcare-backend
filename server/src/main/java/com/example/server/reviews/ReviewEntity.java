package com.example.server.reviews;
import com.example.server.connection.ConnectionEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name= "review")
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int rating;
    private String review;
    @ManyToOne
    @JoinColumn(name = "connectionId")
    private ConnectionEntity conn1;

}
