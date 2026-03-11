package com.rabbitt.salesinsight.dto;

import jakarta.validation.constraints.Email;

public record SummaryMetadata(
        @Email String recipientEmail,
        String subject,
        String instructions
) {}

