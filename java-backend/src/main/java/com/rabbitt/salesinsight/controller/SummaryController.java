package com.rabbitt.salesinsight.controller;

import com.rabbitt.salesinsight.dto.SummaryMetadata;
import com.rabbitt.salesinsight.dto.SummaryResponse;
import com.rabbitt.salesinsight.service.SummaryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/summaries")
public class SummaryController {

    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @Operation(summary = "Upload sales file and trigger AI email summary")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SummaryResponse createSummary(
            @RequestPart("file") MultipartFile file,
            @Valid @RequestPart("metadata") SummaryMetadata metadata
    ) throws IOException {
        return summaryService.handleSummaryRequest(file, metadata);
    }
}

