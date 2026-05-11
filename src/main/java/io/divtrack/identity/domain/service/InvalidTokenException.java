package io.divtrack.identity.domain.service;

import io.divtrack.common.BusinessException;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException(String message) {
        super("INVALID_TOKEN", message, 401);
    }
}
