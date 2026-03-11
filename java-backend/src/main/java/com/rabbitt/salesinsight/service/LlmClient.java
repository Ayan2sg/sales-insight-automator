package com.rabbitt.salesinsight.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class LlmClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.llm.provider}")
    String provider;

    @Value("${app.llm.google-api-key:}")
    String googleKey;

    @Value("${app.llm.groq-api-key:}")
    String groqKey;

    @Value("${app.llm.groq-model}")
    String groqModel;

    public String generateSummary(String insights, String sampleRows, String instructions) {
        String prompt = buildPrompt(insights, sampleRows, instructions);

        return switch (provider.toLowerCase()) {
            case "gemini" -> callGemini(prompt);
            case "groq" -> callGroq(prompt);
            default -> "LLM not configured. Prompt:\\n"
                    + prompt.substring(0, Math.min(1200, prompt.length()));
        };
    }

    private String buildPrompt(String insights, String sampleRows, String instructions) {
        String extra = (instructions == null || instructions.isBlank())
                ? "Write a concise, executive-ready narrative focusing on trends, risks, and opportunities."
                : instructions;
        return """
                You are a senior sales strategist.

                STRUCTURED INSIGHTS:
                %s

                SAMPLE ROWS:
                %s

                GUIDANCE:
                %s
                """.formatted(insights, sampleRows, extra);
    }

    private String callGemini(String prompt) {
        if (googleKey == null || googleKey.isBlank()) {
            throw new IllegalStateException("GOOGLE_API_KEY not configured");
        }
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-goog-api-key", googleKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        ResponseEntity<Map> resp = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        Map data = resp.getBody();
        try {
            Map candidate0 = (Map) ((List<?>) data.get("candidates")).get(0);
            Map content = (Map) candidate0.get("content");
            Map part0 = (Map) ((List<?>) content.get("parts")).get(0);
            return (String) part0.get("text");
        } catch (Exception e) {
            throw new RuntimeException("Unexpected Gemini response: " + data, e);
        }
    }

    private String callGroq(String prompt) {
        if (groqKey == null || groqKey.isBlank()) {
            throw new IllegalStateException("GROQ_API_KEY not configured");
        }
        String url = "https://api.groq.com/openai/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(groqKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "model", groqModel,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful assistant for sales analytics."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.4,
                "max_tokens", 800
        );

        ResponseEntity<Map> resp = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        Map data = resp.getBody();
        try {
            Map choice0 = (Map) ((List<?>) data.get("choices")).get(0);
            Map msg = (Map) choice0.get("message");
            return (String) msg.get("content");
        } catch (Exception e) {
            throw new RuntimeException("Unexpected Groq response: " + data, e);
        }
    }
}

