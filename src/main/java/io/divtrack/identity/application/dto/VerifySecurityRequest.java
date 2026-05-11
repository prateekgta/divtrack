package io.divtrack.identity.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record VerifySecurityRequest(
        @NotBlank @Email String email,
        @Size(min = 2, max = 2) List<@NotBlank String> answers
) {}
