package io.divtrack.identity.application.dto;

import java.util.List;

public record ForgotPasswordResponse(
        String email,
        List<String> questions
) {}
