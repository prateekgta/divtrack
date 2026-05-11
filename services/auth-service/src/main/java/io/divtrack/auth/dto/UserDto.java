package io.divtrack.auth.dto;

import io.divtrack.auth.model.User;

/**
 * Safe user representation for API responses.
 * NEVER return the User entity directly — it contains passwordHash.
 */
public record UserDto(
        String id,
        String email,
        String name,
        String plan,
        boolean emailVerified
) {
    /** Factory method: map User entity → UserDto. */
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPlan(),
                user.isEmailVerified()
        );
    }
}
