package io.divtrack.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Java Records (Java 16+) = perfect for immutable DTOs.
 * No need for @Getter/@Setter/@AllArgsConstructor/@EqualsAndHashCode boilerplate.
 * Validation annotations on record components work with Spring Validation.
 */
public record RegisterRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        String email,

        /**
         * min=8: baseline security.
         * We don't enforce complexity rules here — passphrases beat complex-but-short passwords.
         * BCrypt cost=12 in AuthService ensures brute-force is expensive regardless.
         */
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be 100 characters or less")
        String name
) {}
