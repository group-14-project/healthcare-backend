package com.example.server.specialization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecializationRepository extends JpaRepository<SpecializationEntity, Integer> {
    SpecializationEntity findSpecializationEntityByName(String name);
}
