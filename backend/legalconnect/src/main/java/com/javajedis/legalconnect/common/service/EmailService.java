package com.javajedis.legalconnect.common.service;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String fromEmail;

    public EmailService(JavaMailSender mailSender,
                        TemplateEngine templateEngine,
                        @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.fromEmail = fromEmail;
    }

    /**
     * Send a simple text email (for OTP, notifications, etc.)
     */
    public void sendSimpleEmail(String to, String subject, String body) {
        if (to == null || to.isEmpty() || !EMAIL_PATTERN.matcher(to).matches()) {
            log.warn("Invalid email address: {}", to);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("Sent simple email to {} with subject {}", to, subject);
    }

    /**
     * Send an email using a template (for OTP, notifications, etc.)
     */
    public void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        if (to == null || to.isEmpty() || !EMAIL_PATTERN.matcher(to).matches()) {
            log.warn("Invalid email address: {}", to);
            return;
        }
        Context context = new Context();
        if (variables != null) {
            context.setVariables(variables);
        }
        String htmlContent = templateEngine.process(templateName, context);
        if (htmlContent == null) {
            htmlContent = "";
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("Sent template email to {} with subject {} using template {}", to, subject, templateName);
        } catch (MessagingException | org.springframework.mail.MailException e) {
            log.error("Failed to send template email to {}: {}", to, e.getMessage());
        }
    }
}
