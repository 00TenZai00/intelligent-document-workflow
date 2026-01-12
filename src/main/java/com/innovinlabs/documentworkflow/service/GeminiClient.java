package com.innovinlabs.documentworkflow.service;


import com.innovinlabs.documentworkflow.Exception.GeminiServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String summarize(String text) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", "Summarize this document:\n\n" + text)
                                    )
                            )
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(requestBody, headers);

            String url = apiUrl + "?key=" + apiKey;

            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});

            return extractText(response.getBody());

        } catch (HttpStatusCodeException e) {
            throw new GeminiServiceException("Gemini API returned " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            throw new GeminiServiceException("Gemini API is unreachable or timed out", e);
        } catch (Exception e) {
            // Fallback for unexpected logic errors
            throw new GeminiServiceException("Unexpected error during Gemini summarization", e);
        }
    }

    @SuppressWarnings("")
    private String extractText(Map<String, Object> response) {

        if (response == null) {
            throw new IllegalStateException("Empty Gemini response");
        }

        Object candidatesObj = response.get("candidates");
        if (!(candidatesObj instanceof List<?> candidates) || candidates.isEmpty()) {
            throw new IllegalStateException("No candidates in Gemini response");
        }

        Object firstCandidate = candidates.getFirst();
        if (!(firstCandidate instanceof Map<?, ?> candidateMap)) {
            throw new IllegalStateException("Invalid candidate format");
        }

        Object contentObj = candidateMap.get("content");
        if (!(contentObj instanceof Map<?, ?> contentMap)) {
            throw new IllegalStateException("Missing content in Gemini response");
        }

        Object partsObj = contentMap.get("parts");
        if (!(partsObj instanceof List<?> parts) || parts.isEmpty()) {
            throw new IllegalStateException("No parts in Gemini response");
        }

        Object firstPart = parts.getFirst();
        if (!(firstPart instanceof Map<?, ?> partMap)) {
            throw new IllegalStateException("Invalid part format");
        }

        Object textObj = partMap.get("text");
        if (!(textObj instanceof String text)) {
            throw new IllegalStateException("No text in Gemini response");
        }

        return text;
    }

}
