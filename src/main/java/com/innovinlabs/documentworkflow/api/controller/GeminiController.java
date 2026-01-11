package com.innovinlabs.documentworkflow.api.controller;

import com.innovinlabs.documentworkflow.service.SummarizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class GeminiController {

    private final SummarizationService summarizationService;

    @PostMapping("/{id}/summarize")
    public ResponseEntity<String> summarize(@PathVariable Long id) {
        return ResponseEntity.ok(summarizationService.summarizeDocument(id));
    }
}
