package com.example.server.patient;

import com.example.server.aws.AwsServiceImplementation;
import com.example.server.report.ReportEntity;
import com.example.server.report.ReportRepository;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class PatientScheduledTask
{
    private final PatientRepository patientRepository;
    private final ReportRepository reportRepository;


    private final AwsServiceImplementation awsServiceImplementation;

    public PatientScheduledTask(PatientRepository patientRepository, ReportRepository reportRepository, AwsServiceImplementation awsServiceImplementation)
    {
        this.patientRepository = patientRepository;
        this.reportRepository = reportRepository;
        this.awsServiceImplementation = awsServiceImplementation;
    }


    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteData()
    {
        LocalDate thresholdDate = LocalDate.now().minusDays(730);
        List<PatientEntity>patientEntities=patientRepository.findByDeletionTimeBefore(thresholdDate);
        if(patientEntities!=null)
        {
            for (PatientEntity patientEntity : patientEntities)
            {
                List<ReportEntity> reportEntities = reportRepository.findByPatId(patientEntity.getId());
                if (reportEntities != null)
                {
                    for(ReportEntity reportEntity:reportEntities)
                    {
                        String bucketName = "adityavit36";
                        awsServiceImplementation.deleteFile(bucketName,reportEntity.getFileName());
                        reportRepository.deleteById(reportEntity.getId());
                    }
                }
                patientEntity.setHeight("null");
                patientEntity.setWeight("null");
                patientEntity.setDeletionTime(LocalDate.MAX);
                patientEntity.setAddress("null");
                patientEntity.setGender("null");
                patientEntity.setBloodGroup("null");
                patientEntity.setCity("null");
                patientEntity.setPhoneNumber("null");
                patientEntity.setPinCode("null");
                patientRepository.save(patientEntity);
            }
        }
    }

}
