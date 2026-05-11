package io.divtrack.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when email not found OR password doesn't match.
 *
 * SECURITY: Always return the SAME error message for both cases.
 * "Email not found" vs "Wrong password" lets attackers enumerate valid emails.
 * "Invalid credentials" reveals nothing about which field is wrong.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
