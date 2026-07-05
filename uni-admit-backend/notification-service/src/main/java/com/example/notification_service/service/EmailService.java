package com.example.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ── Send email using Thymeleaf template ───────────────────────────────────
    public void sendEmail(String to, String subject, String templateName,
                          Context context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);

            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent to: {} with subject: {}", to, subject);

        } catch (Exception e) {
            // Log but don't throw — email failure shouldn't crash the listener
            log.error("Failed to send email to: {} — {}", to, e.getMessage());
        }
    }

    // ── Simple text email (fallback when no template needed) ──────────────────
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
            log.info("Simple email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {} — {}", to, e.getMessage());
        }
    }
}
