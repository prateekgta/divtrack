package io.divtrack.auth.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralised error handling — translates exceptions to RFC 7807 Problem Details.
 *
 * WHY ProblemDetail?
 * ──────────────────
 * Spring 6 / Boot 3 ships RFC 7807 support out of the box.
 * Instead of inventing our own {"error":"...", "message":"..."} format,
 * we use the standard:
 * {
 *   "type": "about:blank",
 *   "title": "Conflict",
 *   "status": 409,
 *   "detail": "Email already registered: alice@example.com"
 * }
 *
 * This is what the frontend expects and what monitoring tools understand.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 409 — duplicate email on register */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailExists(EmailAlreadyExistsException ex) {
        log.debug("[Auth] Registration conflict: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Email Already Registered");
        pd.setType(URI.create("about:blank"));
        return pd;
    }

    /** 401 — wrong email/password or bad refresh token */
    @ExceptionHandler({InvalidCredentialsException.class, InvalidTokenException.class})
    public ProblemDetail handleUnauthorized(RuntimeException ex) {
        log.debug("[Auth] Unauthorized: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        pd.setTitle("Unauthorized");
        pd.setType(URI.create("about:blank"));
        return pd;
    }

    /**
     * 400 — @Valid/@Validated constraint violations.
     *
     * Returns field-level errors so the UI can highlight specific fields:
     * {
     *   "status": 400,
     *   "detail": "Validation failed",
     *   "errors": {
     *     "email": "Must be a valid email address",
     *     "password": "Password must be at least 8 characters"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing  // keep first error per field
                ));

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setTitle("Bad Request");
        pd.setType(URI.create("about:blank"));
        pd.setProperty("errors", errors);
        return pd;
    }

    /** 500 — unexpected errors (log with stack trace, return safe message) */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("[Auth] Unexpected error", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        pd.setTitle("Internal Server Error");
        pd.setType(URI.create("about:blank"));
        return pd;
    }
}
