package com.example.server.emailOtp;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

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

}
