package com.chatup.backend.service;

import com.chatup.backend.models.PasswordResetToken;
import com.chatup.backend.repositories.PasswordResetTokenRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender javaMailSender;

    public PasswordResetService(PasswordResetTokenRepository passwordResetTokenRepository, JavaMailSender javaMailSender) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.javaMailSender = javaMailSender;
    }

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
                + "https://effortless-cucurucho-949d34.netlify.app/change-password?token=" + token +
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
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isPresent()) {
            PasswordResetToken prt = tokenOpt.get();
            prt.setExpirationDate(new Date());
            passwordResetTokenRepository.save(prt);
            passwordResetTokenRepository.delete(prt);
        }
    }
}
