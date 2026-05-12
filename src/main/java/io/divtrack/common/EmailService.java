package io.divtrack.common;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true", matchIfMissing = false)
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String resetToken) {
        String subject = "DivTrack — Password Reset Request";
        String link = "http://localhost:3000/reset-password?token=" + resetToken;
        String body = """
            <html>
            <body style="font-family: sans-serif; padding: 24px;">
                <h2 style="color: #166534;">DivTrack Password Reset</h2>
                <p>You requested a password reset. Click the link below to set a new password:</p>
                <p><a href="%s" style="display: inline-block; padding: 12px 24px; background: #16a34a; color: white; text-decoration: none; border-radius: 8px;">Reset Password</a></p>
                <p>This link expires in 15 minutes.</p>
                <p>If you didn't request this, ignore this email.</p>
            </body>
            </html>
            """.formatted(link);

        sendHtml(to, subject, body);
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Email sent to {}", to);
        } catch (MailException | MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
