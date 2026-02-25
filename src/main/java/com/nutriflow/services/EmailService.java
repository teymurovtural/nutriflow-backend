package com.nutriflow.services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.*;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name}")
    private String fromName;

    @Async
    public void sendVerificationEmail(String to, String otp) {
        log.info("Email sending process started. Recipient: {}", to);
        String subject = "NutriFlow - Verification Code";
        String body = "Hello!\n\n" +
                "Thank you for joining NutriFlow. " +
                "Your verification code to activate your account is: " + otp + "\n\n" +
                "This code is valid for 5 minutes.\n\n" +
                "Best regards,\nNutriFlow Team";
        sendEmail(to, subject, body);
    }

    @Async
    public void sendForgotPasswordEmail(String to, String otp) {
        log.info("Forgot password email sending started. Recipient: {}", to);
        String subject = "NutriFlow - Password Reset Code";
        String body = "Hello!\n\n" +
                "We received a request to reset your NutriFlow account password.\n" +
                "Your password reset code is: " + otp + "\n\n" +
                "This code is valid for 5 minutes.\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Best regards,\nNutriFlow Team";
        sendEmail(to, subject, body);
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            Email from = new Email(fromEmail, fromName);
            Email toEmail = new Email(to);
            Content content = new Content("text/plain", body);
            Mail mail = new Mail(from, subject, toEmail, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 400) {
                log.error("Email sending failed! Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
            } else {
                log.info("Email sent successfully to: {}", to);
            }
        } catch (IOException e) {
            log.error("Email sending error! Recipient: {}, Error: {}", to, e.getMessage());
        }
    }
}