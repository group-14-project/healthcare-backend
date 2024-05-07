package com.example.server.report;

import com.example.server.connection.ConnectionEntity;
import com.example.server.dto.response.ReportDetailsResponse;
import com.example.server.patient.PatientEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {
    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public ReportEntity addReport(String fileName, PatientEntity patientEntity, ConnectionEntity connectionEntity, String reportName) {
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setFileName(fileName);
        reportEntity.setPat(patientEntity);
        reportEntity.setReportName(reportName);
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

    public List<ReportDetailsResponse> findAllReportsByConnection(ConnectionEntity connectionEntity) {
        List<ReportEntity> reportEntities = reportRepository.findAllByConnection(connectionEntity);
        List<ReportDetailsResponse> reportDetailsResponses = new ArrayList<>();
        for(ReportEntity reportEntity : reportEntities){
            ReportDetailsResponse response = new ReportDetailsResponse();
            response.setReportName(reportEntity.getReportName());
            response.setId(reportEntity.getId());
            response.setLocalDateTime(reportEntity.getDateTime());
            if (reportEntity.getCon() != null) {
                response.setDoctorFirstName(reportEntity.getCon().getDoctor().getFirstName());
                response.setDoctorLastName(reportEntity.getCon().getDoctor().getLastName());
            }
            reportDetailsResponses.add(response);

        }
        return reportDetailsResponses;
    }

    public List<ReportDetailsResponse> findAllReportsByConnectionRecommended(ConnectionEntity connectionEntity) {
        List<ReportEntity> reportEntities = reportRepository.findAllByConnectionNotNull(connectionEntity);
        List<ReportDetailsResponse> reportDetailsResponses = new ArrayList<>();
        for(ReportEntity reportEntity : reportEntities){
            ReportDetailsResponse response = new ReportDetailsResponse();
            response.setReportName(reportEntity.getReportName());
            response.setId(reportEntity.getId());
            response.setLocalDateTime(reportEntity.getDateTime());
            if (reportEntity.getCon() != null) {
                response.setDoctorFirstName(reportEntity.getCon().getDoctor().getFirstName());
                response.setDoctorLastName(reportEntity.getCon().getDoctor().getLastName());
            }
            reportDetailsResponses.add(response);

        }
        return reportDetailsResponses;
    }

    public List<ReportDetailsResponse> findAllReportsByConnectionListAndBlank(List<ConnectionEntity> connectionEntities, PatientEntity patientEntity) {
        List<ReportEntity> reportEntities = reportRepository.findAllByConnectionsInOrConnectionsIsNull(connectionEntities);
        List<ReportDetailsResponse> reportDetailsResponses = new ArrayList<>();
        for(ReportEntity reportEntity : reportEntities){
            if(reportEntity.getPat()==patientEntity) {
                ReportDetailsResponse response = new ReportDetailsResponse();
                response.setReportName(reportEntity.getReportName());
                response.setId(reportEntity.getId());
                response.setLocalDateTime(reportEntity.getDateTime());
                if (reportEntity.getCon() != null) {
                    response.setDoctorFirstName(reportEntity.getCon().getDoctor().getFirstName());
                    response.setDoctorLastName(reportEntity.getCon().getDoctor().getLastName());
                }
                reportDetailsResponses.add(response);
            }
        }
        return reportDetailsResponses;
    }
}
