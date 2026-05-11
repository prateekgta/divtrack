package io.divtrack.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for auth-service.
 *
 * AUTH-SERVICE IS SPECIAL:
 * ─────────────────────────
 * Unlike other services (portfolio, market-data) which VERIFY JWTs,
 * auth-service ISSUES JWTs. It doesn't protect its own /api/auth/** endpoints
 * with JWT — they're the entry point for getting tokens.
 *
 * All /api/auth/** endpoints are public (no authentication required).
 * Everything else is locked down (no other endpoints exist in this service).
 *
 * WHY STATELESS SESSION?
 * ──────────────────────
 * JWT is stateless by design. We never want Spring Security to create an
 * HttpSession — that would waste memory and break horizontal scaling.
 * STATELESS = no session cookie, no server-side session state.
 *
 * WHY DISABLE CSRF?
 * ─────────────────
 * CSRF protection is for session-cookie-based auth (browser sends cookie
 * automatically on every request). Since our API clients send JWTs in
 * Authorization headers (not cookies), CSRF doesn't apply.
 * For the refresh token (if stored in httpOnly cookie), we'd need SameSite=Strict.
 *
 * BCRYPT COST=12:
 * ───────────────
 * Default BCrypt cost is 10. Cost 12 means 2^12 = 4096 iterations.
 * Each hash takes ~250ms on modern hardware.
 * An attacker with a stolen DB trying to brute-force 1 billion passwords
 * would need 250ms × 1B = ~8 years on a single machine.
 * Users notice nothing (register/login is a one-time event, not per-request).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Disable CSRF — REST API with JWT, not browser session cookies
                .csrf(AbstractHttpConfigurer::disable)

                // CORS — allow requests from the frontend during dev
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // No server-side sessions — JWT is our session
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // All /api/auth/** is public — that's the whole point of auth-service
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().denyAll()   // fail closed — no unexpected endpoints
                )
                .build();
    }

    /**
     * BCrypt with cost factor 12.
     *
     * WHY NOT BCrypt(10)?
     * Default cost 10 hashes in ~50ms. Cost 12 hashes in ~250ms.
     * 5× slower for users = negligible. 5× harder for attackers = matters.
     * OWASP recommends cost ≥ 10; cost 12 gives comfortable headroom.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * CORS config: allow the Next.js frontend (port 3000) to call auth endpoints.
     * In production, replace APP_BASE_URL with the Vercel URL.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",        // Next.js dev
                "http://localhost:3001",        // Alt dev port
                "https://*.vercel.app",         // Vercel preview deployments
                "https://divtrack.vercel.app"   // Vercel production
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
