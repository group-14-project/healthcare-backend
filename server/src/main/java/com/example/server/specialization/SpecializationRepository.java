package com.example.server.specialization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecializationRepository extends JpaRepository<SpecializationEntity, Integer> {
    SpecializationEntity findSpecializationEntityByName(String name);

    @Query("select c.name from SpecializationEntity c")
    List<String> findAllNames();
}
