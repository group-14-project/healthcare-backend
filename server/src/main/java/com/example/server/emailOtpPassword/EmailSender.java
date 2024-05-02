package com.example.server.emailOtpPassword;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class EmailSender {
    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private OtpUtil otpUtil;


    public String sendOtpEmail(String email, String name) {
        String otp = otpUtil.generateOtp();
        try {
            MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true, "UTF8");

            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("Arogyashala:");

        String htmlContent = String.format("""
                Hi %s,
                Your One Time Password (OTP) for the AROGYASHALA account is:
                %s
                Regards,
                Team Arogyashala
                """, name, otp);
            mimeMessageHelper.setText(htmlContent);
            javaMailSender.send(mimeMailMessage);
        } catch (MessagingException e) {

            // Notify the user about the error
            String errorMessage = "An error occurred while sending the email. Please try again later.";
            // You can use your application's error handling mechanism to notify the user, such as showing a message on the UI or sending a notification

            // Alternatively, you can rethrow the exception to propagate it further
            throw new RuntimeException(errorMessage, e);
        }
        return otp;
    }

    //this is for sending the email to the hospital with password
    public void sendMailWithPassword(String email,String name,String password)
    {
        try
        {
            MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true, "UTF8");

            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("Arogyashala:");

            String htmlContent = String.format("""
                Hi %s,
                Your Password for the AROGYASHALA account is:
                %s
                Please login with you emailID and password
                Regards,
                Team Arogyashala
                """, name,password);
            mimeMessageHelper.setText(htmlContent);
            javaMailSender.send(mimeMailMessage);
        } catch (MessagingException e) {
            // Notify the user about the error
            String errorMessage = "An error occurred while sending the email. Please try again later.";
            throw new RuntimeException(errorMessage, e);
        }
    }
    public void sendReminderEmail(String email, String name1, String name2, LocalDateTime time){
        try
        {
            MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true, "UTF8");

            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("Arogyashala:");

            String htmlContent = String.format("""
                Hi %s,
                Your have an Appointment in the AROGYASHALA platform with,
                %s at %s.
                Regards,
                Team Arogyashala
                """, name1, name2, time);
            mimeMessageHelper.setText(htmlContent);
            javaMailSender.send(mimeMailMessage);
        } catch (MessagingException e) {
            // Notify the user about the error
            String errorMessage = "An error occurred while sending the email. Please try again later.";
            // You can use your application's error handling mechanism to notify the user, such as showing a message on the UI or sending a notification
            // Alternatively, you can rethrow the exception to propagate it further
            throw new RuntimeException(errorMessage, e);
        }
    }

    public void sendConsentEmailToPatient(String patientEmail,String patientName ,String doctorName, String newDoctorName){
        try
        {
            MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true, "UTF8");

            mimeMessageHelper.setTo(patientEmail);
            mimeMessageHelper.setSubject("Arogyashala:");

            String htmlContent = String.format("""
            Hi %s,
            Your have been referred to Dr.%s in the AROGYASHALA platform by,
            Dr.%s.Please give your consent by Logging in to AROGYASHALA.
            Regards,
            Team Arogyashala
            """, patientName,newDoctorName,doctorName);
            mimeMessageHelper.setText(htmlContent);
            javaMailSender.send(mimeMailMessage);
        } catch (MessagingException e) {
            // Notify the user about the error
            String errorMessage = "An error occurred while sending the email. Please try again later.";
            throw new RuntimeException(errorMessage, e);
        }
    }
    public void sendConsentEmailToSrDoctor(String doctorEmail,String srDoctorName,String patientName ,String doctorName, String newDoctorName){
        try
        {
            MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true, "UTF8");

            mimeMessageHelper.setTo(doctorEmail);
            mimeMessageHelper.setSubject("Arogyashala:");

            String htmlContent = String.format("""
            Hi Dr. %s,
            Patient %s has been referred to Dr.%s in the AROGYASHALA platform by,
            Dr.%s.Please give your consent.
            Regards,
            Team Arogyashala
            """,srDoctorName, patientName,newDoctorName,doctorName);
            mimeMessageHelper.setText(htmlContent);
            javaMailSender.send(mimeMailMessage);
        } catch (MessagingException e) {
            // Notify the user about the error
            String errorMessage = "An error occurred while sending the email. Please try again later.";
            throw new RuntimeException(errorMessage, e);
        }
    }

    public void approvedPatientConsentToMainDoctor(List<String> emails, String patient, String doctor, String newDoctor) {
        for (String email : emails) {
            try {
                MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true, "UTF8");
                mimeMessageHelper.setTo(email);
                mimeMessageHelper.setSubject("Arogyashala: Patient Approved the Consent");

                String htmlContent = String.format("""
                        Hi,
                        Patient %s has approved the consent for sharing their reports to Dr.%s in the AROGYASHALA platform,
                        recommended by Dr.%s.
                        Regards,
                        Team Arogyashala
                        """, patient, newDoctor, doctor);
                mimeMessageHelper.setText(htmlContent);
                javaMailSender.send(mimeMailMessage);
            } catch (MessagingException e) {
                // Notify the user about the error
                String errorMessage = "An error occurred while sending the email. Please try again later.";
                throw new RuntimeException(errorMessage, e);
            }
        }
    }

    public void sendApprovalEmailNewDoctor(String email, String patient, String doctor, String newDoctorName) {
        try
        {
            MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true, "UTF8");

            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("Arogyashala: Patient has been recommended");

            String htmlContent = String.format("""
            Hi Dr. %s,
            Patient %s has been referred you, by Dr.%s in the AROGYASHALA platform by,
            You can check their Reports in the Arogyashala Application.
            Regards,
            Team Arogyashala
            """,newDoctorName, patient,doctor);
            mimeMessageHelper.setText(htmlContent);
            javaMailSender.send(mimeMailMessage);
        } catch (MessagingException e) {
            // Notify the user about the error
            String errorMessage = "An error occurred while sending the email. Please try again later.";
            throw new RuntimeException(errorMessage, e);
        }
    }

    public void approvedSrDoctorConsentToMainDoctor(List<String> emails, String patient, String doctor, String newDoctor) {
        for (String email : emails) {
            try {
                MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true, "UTF8");
                mimeMessageHelper.setTo(email);
                mimeMessageHelper.setSubject("Arogyashala: Patient Approved the Consent");

                String htmlContent = String.format("""
                        Hi,
                        Senior Doctor has approved the consent for sharing the reports of Patient %s
                        to Dr.%s in the AROGYASHALA platform,
                        recommended by Dr.%s.
                        Regards,
                        Team Arogyashala
                        """, patient, newDoctor, doctor);
                mimeMessageHelper.setText(htmlContent);
                javaMailSender.send(mimeMailMessage);
            } catch (MessagingException e) {
                // Notify the user about the error
                String errorMessage = "An error occurred while sending the email. Please try again later.";
                throw new RuntimeException(errorMessage, e);
            }
        }
    }

    public void sendRejectionEmailToDoctor(String email, String newDoctor, String patientName) {
        try
        {
            MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true, "UTF8");

            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("Arogyashala: Patient has been recommended");

            String htmlContent = String.format("""
            Hi,
            The request to send details of Patient %s to Dr.%s has be rejected.
            Get more info about it in the Arogyashala Application.
            Regards,
            Team Arogyashala
            """,patientName, newDoctor);
            mimeMessageHelper.setText(htmlContent);
            javaMailSender.send(mimeMailMessage);
        } catch (MessagingException e) {
            // Notify the user about the error
            String errorMessage = "An error occurred while sending the email. Please try again later.";
            throw new RuntimeException(errorMessage, e);
        }
    }
}
