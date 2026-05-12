package io.divtrack.identity.application.service;

import io.divtrack.common.EmailService;
import io.divtrack.identity.application.dto.*;
import io.divtrack.identity.domain.model.User;
import io.divtrack.identity.domain.service.AuthDomainService;
import io.divtrack.identity.domain.service.AuthDomainService.TokenPair;
import io.divtrack.identity.domain.service.AuthDomainService.RefreshResult;
import io.divtrack.identity.infrastructure.security.JwtProvider;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityApplicationService {

    private final AuthDomainService authDomain;
    private final JwtProvider jwtProvider;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        User user = authDomain.register(req.email(), req.password(), req.name(),
                req.securityQuestion1(), req.securityAnswer1(),
                req.securityQuestion2(), req.securityAnswer2());
        return buildResponse(user, null);
    }

    @Transactional
    public AuthResponse login(LoginRequest req, String deviceInfo) {
        User user = authDomain.login(req.email(), req.password());
        return buildResponse(user, deviceInfo);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        RefreshResult result = authDomain.rotateRefreshToken(req.refreshToken());
        return buildResponse(result.user(), null);
    }

    @Transactional
    public void logout(RefreshRequest req) {
        authDomain.logout(req.refreshToken());
    }

    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest req) {
        try {
            User user = authDomain.findByEmail(req.email());
            String resetToken = authDomain.initiatePasswordReset(user);
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        } catch (Exception e) {
            log.warn("Password reset requested for unknown email: {}", req.email());
        }
        return new ForgotPasswordResponse(req.email(),
                "If the account exists, a reset link has been sent.");
    }

    @Transactional
    public VerifySecurityResponse verifySecurity(VerifySecurityRequest req) {
        User user = authDomain.findByEmail(req.email());
        if (!user.hasSecurityQuestions()) {
            throw new IllegalStateException("No security questions configured");
        }
        List<String> answers = req.answers();
        if (answers.size() != 2
                || !user.verifySecurityAnswer(1, answers.get(0))
                || !user.verifySecurityAnswer(2, answers.get(1))) {
            throw new IllegalArgumentException("Incorrect answers");
        }
        String resetToken = authDomain.initiatePasswordReset(user);
        return new VerifySecurityResponse(resetToken);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        authDomain.resetPassword(req.resetToken(), req.newPassword());
    }

    private AuthResponse buildResponse(User user, String deviceInfo) {
        String accessToken = jwtProvider.generateAccessToken(user);
        TokenPair refresh = authDomain.generateRefreshToken(user, deviceInfo);
        return new AuthResponse(
                accessToken,
                refresh.rawToken(),
                "Bearer",
                jwtProvider.expirySeconds(),
                UserDto.from(user)
        );
    }
}
