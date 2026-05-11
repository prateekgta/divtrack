package io.divtrack.identity.domain.service;

import io.divtrack.common.BusinessException;

public class InvalidCredentialsException extends BusinessException {
    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", "Invalid email or password", 401);
    }
}
