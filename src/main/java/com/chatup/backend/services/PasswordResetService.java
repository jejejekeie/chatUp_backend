package com.chatup.backend.services;

import com.chatup.backend.models.PasswordResetToken;
import com.chatup.backend.repositories.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    public void generatePasswordResetToken(String userEmail) {
        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(userEmail);
        token.setToken(UUID.randomUUID().toString());
        token.setExpirationDate(getExpirationDate());

        passwordResetTokenRepository.save(token);

        sendPasswordResetEmail(userEmail, token.getToken());
    }

    private void sendPasswordResetEmail(String userEmail, String token) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(userEmail);
        mailMessage.setSubject("Password Reset Request");
        mailMessage.setText("To reset your password, click the link below:\n"
                + "http://localhost:8080/api/auth/change-password?token=" + token +
                    "\n\nIf you did not request a password reset, please ignore this email.");
        javaMailSender.send(mailMessage);
    }

    public boolean validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        return tokenOpt.isPresent() && tokenOpt.get().getExpirationDate().after(new Date());
    }

    private Date getExpirationDate() {
        Date now = new Date();
        long time = now.getTime();
        return new Date(time + 24 * 60 * 60 * 1000);
    }

    public PasswordResetToken getTokenDetails(String token) {
        return passwordResetTokenRepository.findByToken(token).orElse(null);
    }

    public void invalidate(String token) {
        passwordResetTokenRepository.deleteByToken(token);
    }
}
