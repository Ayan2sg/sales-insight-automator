package com.rabbitt.salesinsight.dto;

import java.time.Instant;

public record SummaryResponse(
        String requestId,
        String recipientEmail,
        String status,
        Instant createdAt
) {}

