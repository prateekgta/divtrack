package io.divtrack.identity.domain.service;

import io.divtrack.common.BusinessException;

public class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException(String email) {
        super("EMAIL_EXISTS", "Email already registered: " + email, 409);
    }
}
