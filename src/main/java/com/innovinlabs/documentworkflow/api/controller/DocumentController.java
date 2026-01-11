package com.innovinlabs.documentworkflow.api.controller;

import com.innovinlabs.documentworkflow.api.dto.CreateDocumentRequest;
import com.innovinlabs.documentworkflow.api.dto.DocumentResponse;
import com.innovinlabs.documentworkflow.entity.Document;
import com.innovinlabs.documentworkflow.service.DocumentService;
import com.innovinlabs.documentworkflow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateDocumentRequest request
    ) {
        String email = userDetails.getUsername();
        Long userId = userService.getUserIdByEmail(email);

        Document document = documentService.createDocument(
                request.getTitle(),
                request.getContent(),
                userId,
                request.getSignerId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(document));
    }

    @GetMapping("/owned")
    public List<DocumentResponse> getOwnedDocuments(
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        String email = userDetails.getUsername();
        Long userId = userService.getUserIdByEmail(email);
        return documentService.getDocumentsOwnedBy(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/to-sign")
    public List<DocumentResponse> getDocumentsToSign(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        Long userId = userService.getUserIdByEmail(email);
        return documentService.getDocumentsToSign(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{documentId}")
    public DocumentResponse getDocument(
            @PathVariable Long documentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        Long userId = userService.getUserIdByEmail(email);
        Document document = documentService.getDocumentForOwner(documentId, userId);
        return toResponse(document);
    }

    private DocumentResponse toResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .status(document.getStatus())
                .ownerId(document.getOwner().getId())
                .signerId(document.getSigner().getId())
                .createdAt(document.getCreatedAt())
                .signedAt(document.getSignedAt())
                .build();
    }
}

