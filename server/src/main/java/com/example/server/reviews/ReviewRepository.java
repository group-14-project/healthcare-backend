package com.example.server.reviews;

import com.example.server.connection.ConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Integer> {
    Optional<ReviewEntity> findByConn1(ConnectionEntity connection);
}
