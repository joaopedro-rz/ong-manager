package com.ongmanager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.base-url}") private String baseUrl;
    @Value("${spring.mail.username}") private String from;

    public void sendVerification(String to, String token) {
        send(to, "Confirme seu cadastro",
            "Bem-vindo! Confirme seu e-mail acessando: " + baseUrl + "/verify-email?token=" + token);
    }

    public void sendPasswordReset(String to, String token) {
        send(to, "Recuperacao de senha",
            "Redefina sua senha em: " + baseUrl + "/reset-password?token=" + token + " (validade 1 hora)");
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setFrom(from);
            m.setTo(to);
            m.setSubject(subject);
            m.setText(body);
            mailSender.send(m);
        } catch (Exception ex) {
            log.warn("Falha ao enviar e-mail para {}: {}", to, ex.getMessage());
        }
    }
}
