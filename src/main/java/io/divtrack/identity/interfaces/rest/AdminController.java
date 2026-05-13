package io.divtrack.identity.interfaces.rest;

import io.divtrack.identity.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @PostMapping("/upgrade/{email}")
    public ResponseEntity<?> upgrade(@PathVariable String email) {
        log.info("Admin upgrade requested for: {}", email);
        var normalized = email.toLowerCase().trim();
        var userOpt = userRepository.findByEmail(normalized);
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", normalized);
            return ResponseEntity.notFound().build();
        }
        var user = userOpt.get();
        log.info("Found user: {}, current plan: {}", user.getEmail(), user.getPlan());
        user.upgradeToPro();
        userRepository.save(user);
        log.info("Upgraded {} to PRO", email);
        return ResponseEntity.ok("Upgraded " + email + " to PRO");
    }
}
