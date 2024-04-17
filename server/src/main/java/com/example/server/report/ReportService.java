package com.example.server.report;

import com.example.server.connection.ConnectionEntity;
import com.example.server.patient.PatientEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReportService {
    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public ReportEntity addReport(String fileName, PatientEntity patientEntity, ConnectionEntity connectionEntity) {
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setFileName(fileName);
        reportEntity.setPat(patientEntity);
        if(connectionEntity!=null)reportEntity.setCon(connectionEntity);
        reportEntity.setDateTime(LocalDateTime.now());
        return reportRepository.save(reportEntity);

    }

    public ReportEntity findReportById(Integer id) {
        ReportEntity entity = reportRepository.findByReportId(id);
        if(entity==null){
            return null;
        }
        return entity;
    }
}
