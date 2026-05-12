package io.divtrack.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class NoopEmailService {

    public void sendPasswordResetEmail(String to, String resetToken) {
        log.warn("Email disabled — password reset token for {}: {}", to, resetToken);
        log.warn("In production, this would send an email with the reset link.");
    }
}
