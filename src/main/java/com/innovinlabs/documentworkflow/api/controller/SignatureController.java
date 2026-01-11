package com.innovinlabs.documentworkflow.api.controller;


import com.innovinlabs.documentworkflow.api.dto.OtpRequest;
import com.innovinlabs.documentworkflow.service.SignatureService;
import com.innovinlabs.documentworkflow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class SignatureController {

    private final SignatureService signatureService;
    private final UserService userService;

    /**
     * Request OTP for signing a document.
     */
    @PostMapping("/{documentId}/sign/otp")
    public ResponseEntity<Void> requestOtp(
            @PathVariable Long documentId,
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        String email = userDetails.getUsername();
        Long signerId = userService.getUserIdByEmail(email);
        signatureService.requestOtp(documentId, signerId);
        return ResponseEntity.accepted().build();
    }

    /**
     * Verify OTP and sign the document.
     */
    @PostMapping("/{documentId}/sign")
    public ResponseEntity<Void> signDocument(
            @PathVariable Long documentId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OtpRequest request
    ) {
        String email = userDetails.getUsername();
        Long signerId = userService.getUserIdByEmail(email);
        signatureService.signDocument(documentId, signerId, request.getOtp());
        return ResponseEntity.ok().build();
    }


}

