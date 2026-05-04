package com.queuesense.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 🔥 GENERIC MAIL (use anywhere)
    public void sendMail(String to, String subject, String body) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    // 🔥 OTP MAIL
    public void sendOtpMail(String to, String otp) {

        String subject = "QueueSense OTP Verification";

        String body = "Your OTP is: " + otp + "\n\nDo not share this with anyone.";

        sendMail(to, subject, body);
    }

    // 🔥 TURN ALERT MAIL
    public void sendTurnAlert(String to, String name) {

        String subject = "Your Turn is Next";

        String body = "Hello " + name + ", your turn is coming soon. Please be ready.";

        sendMail(to, subject, body);
    }
}