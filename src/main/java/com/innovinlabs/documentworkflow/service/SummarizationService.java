package com.innovinlabs.documentworkflow.service;

import com.innovinlabs.documentworkflow.entity.Document;
import com.innovinlabs.documentworkflow.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SummarizationService {

    private final DocumentRepository documentRepository;
    private final GeminiClient geminiClient;

    public String summarizeDocument(Long documentId) {

        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        return geminiClient.summarize(doc.getContent());
    }
}
