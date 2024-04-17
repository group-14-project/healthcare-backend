package com.example.server.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReportRepository extends JpaRepository<ReportEntity,Integer> {
    @Query("select c from ReportEntity c where c.id =:id")
    ReportEntity findByReportId(Integer id);
}
