package io.divtrack.auth.dto;

/**
 * Returned by /register, /login, and /refresh endpoints.
 *
 * TOKEN STORAGE RECOMMENDATION (for the frontend team):
 * ──────────────────────────────────────────────────────
 * accessToken  → memory only (JS variable, never localStorage)
 *                Short-lived (15 min) — XSS can steal it but it expires fast
 *
 * refreshToken → httpOnly cookie set by the server (not accessible to JS)
 *                Long-lived (7 days) — CSRF-protected because it's in a cookie,
 *                not in the Authorization header
 *
 * For simplicity at MVP, we return both in the body.
 * Production hardening: move refreshToken to Set-Cookie: httpOnly; Secure; SameSite=Strict
 *
 * expiresIn: seconds until accessToken expires (frontend uses this to schedule silent refresh)
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,     // always "Bearer"
        long   expiresIn,     // seconds
        UserDto user
) {}
