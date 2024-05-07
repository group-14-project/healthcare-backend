package com.example.server.report;

import com.example.server.connection.ConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportRepository extends JpaRepository<ReportEntity,Integer> {
    @Query("select c from ReportEntity c where c.id =:id")
    ReportEntity findByReportId(Integer id);

    @Query("select c from ReportEntity c where c.con =:connectionEntity OR c.con IS NULL")
    List<ReportEntity> findAllByConnection(ConnectionEntity connectionEntity);


    @Query("select c from ReportEntity c where c.con in :connectionEntities or c.con is null")
    List<ReportEntity> findAllByConnectionsInOrConnectionsIsNull(List<ConnectionEntity> connectionEntities);

     List<ReportEntity> findByPatId(Integer patientId);

    @Query("select c from ReportEntity c where c.con =:connectionEntity")
    List<ReportEntity> findAllByConnectionNotNull(ConnectionEntity connectionEntity);
}
