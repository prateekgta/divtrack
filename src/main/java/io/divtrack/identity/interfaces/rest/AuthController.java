package io.divtrack.identity.interfaces.rest;

import io.divtrack.identity.application.dto.*;
import io.divtrack.identity.application.service.IdentityApplicationService;
import io.divtrack.identity.domain.port.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IdentityApplicationService appService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req,
                                              HttpServletRequest request) {
        String deviceInfo = request.getHeader("User-Agent");
        return ResponseEntity.ok(appService.login(req, deviceInfo));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(appService.refresh(req));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req) {
        appService.logout(req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        return ResponseEntity.ok(appService.forgotPassword(req));
    }

    @PostMapping("/verify-security")
    public ResponseEntity<VerifySecurityResponse> verifySecurity(@Valid @RequestBody VerifySecurityRequest req) {
        return ResponseEntity.ok(appService.verifySecurity(req));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        appService.resetPassword(req);
        return ResponseEntity.ok().build();
    }

    // TEMPORARY: upgrade user to Pro
    @PostMapping("/admin/upgrade/{email}")
    public ResponseEntity<?> adminUpgrade(@PathVariable String email) {
        var user = userRepository.findByEmail(email.toLowerCase().trim());
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        user.get().upgradeToPro();
        userRepository.save(user.get());
        return ResponseEntity.ok("Upgraded " + email + " to PRO");
    }
}
