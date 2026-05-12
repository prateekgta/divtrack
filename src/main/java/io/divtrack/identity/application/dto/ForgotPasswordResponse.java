package io.divtrack.identity.application.dto;

public record ForgotPasswordResponse(
        String email,
        String message
) {}
