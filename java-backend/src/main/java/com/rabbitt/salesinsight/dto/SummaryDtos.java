package com.rabbitt.salesinsight.dto;

import jakarta.validation.constraints.Email;

import java.time.Instant;

public record SummaryMetadata(
        @Email String recipientEmail,
        String subject,
        String instructions
) {}

public record SummaryResponse(
        String requestId,
        String recipientEmail,
        String status,
        Instant createdAt
) {}

