package io.divtrack.identity.application.service;

import io.divtrack.identity.application.dto.*;
import io.divtrack.identity.domain.model.User;
import io.divtrack.identity.domain.service.AuthDomainService;
import io.divtrack.identity.domain.service.AuthDomainService.TokenPair;
import io.divtrack.identity.domain.service.AuthDomainService.RefreshResult;
import io.divtrack.identity.infrastructure.security.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdentityApplicationService {

    private final AuthDomainService authDomain;
    private final JwtProvider jwtProvider;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        User user = authDomain.register(req.email(), req.password(), req.name());
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
