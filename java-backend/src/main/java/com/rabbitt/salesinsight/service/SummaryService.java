package com.rabbitt.salesinsight.service;

import com.rabbitt.salesinsight.dto.SummaryMetadata;
import com.rabbitt.salesinsight.dto.SummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Service
public class SummaryService {

    private final SalesParser salesParser;
    private final LlmClient llmClient;
    private final EmailSender emailSender;

    public SummaryService(SalesParser salesParser, LlmClient llmClient, EmailSender emailSender) {
        this.salesParser = salesParser;
        this.llmClient = llmClient;
        this.emailSender = emailSender;
    }

    public SummaryResponse handleSummaryRequest(MultipartFile file, SummaryMetadata metadata) throws IOException {
        String requestId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        SalesInsights insights = salesParser.parse(file);
        String structured = insights.toSummaryString();
        String sampleRows = insights.sampleRowsAsString(5);

        String narrative = llmClient.generateSummary(structured, sampleRows, metadata.instructions());

        String subject = (metadata.subject() == null || metadata.subject().isBlank())
                ? "Quarterly Sales Insight Summary"
                : metadata.subject();

        emailSender.send(metadata.recipientEmail(), subject, narrative);

        return new SummaryResponse(requestId, metadata.recipientEmail(), "sent", now);
    }
}

