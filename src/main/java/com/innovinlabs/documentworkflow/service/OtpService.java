package com.innovinlabs.documentworkflow.service;


import com.innovinlabs.documentworkflow.entity.Document;
import com.innovinlabs.documentworkflow.entity.SignatureOtp;
import com.innovinlabs.documentworkflow.entity.User;
import com.innovinlabs.documentworkflow.repository.DocumentRepository;
import com.innovinlabs.documentworkflow.repository.SignatureOtpRepository;
import com.innovinlabs.documentworkflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;

    private final SignatureOtpRepository otpRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Generate and send OTP to signer email.
     */
    @Transactional
    public void generateAndSendOtp(Long documentId, Long signerId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        User signer = userRepository.findById(signerId)
                .orElseThrow(() -> new IllegalArgumentException("Signer not found"));

        // Ensure no active OTP exists
        otpRepository
                .findByDocumentIdAndSignerIdAndUsedFalseAndExpiresAtAfter(
                        documentId,
                        signerId,
                        Instant.now()
                )
                .ifPresent(existing -> {
                    throw new IllegalStateException("An active OTP already exists");
                });


        String rawOtp = generateOtp();
        String hashedOtp = passwordEncoder.encode(rawOtp);

        SignatureOtp otp = SignatureOtp.builder()
                .document(document)
                .signer(signer)
                .otpHash(hashedOtp)
                .expiresAt(Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES))
                .used(false)
                .attemptCount(0)
                .createdAt(Instant.now())
                .build();

        otpRepository.save(otp);

        sendOtpEmail(signer.getEmail(), rawOtp, document.getTitle());
    }

    /**
     * Verify OTP during signing.
     */
    @Transactional
    public void verifyOtp(Long documentId, Long signerId, String providedOtp) {

        SignatureOtp otp = otpRepository.findByDocumentIdAndSignerIdAndUsedFalse(documentId, signerId)
                .orElseThrow(() -> new IllegalArgumentException("No active OTP found"));

        if (otp.getExpiresAt().isBefore(Instant.now())) {
            otp.setExpired(true);
            throw new IllegalStateException("OTP has expired");
        }

        if (otp.getAttemptCount() >= MAX_ATTEMPTS) {
            throw new IllegalStateException("Maximum OTP attempts exceeded");
        }

        if (!passwordEncoder.matches(providedOtp, otp.getOtpHash())){
            otp.setAttemptCount(otp.getAttemptCount() + 1);
            throw new IllegalArgumentException("Invalid OTP");
        }

        otp.setUsed(true);
        otp.setVerifiedAt(Instant.now());
    }

    // =========================
    // Helper Methods
    // =========================

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = random.nextInt((int) Math.pow(10, OTP_LENGTH));
        return String.format("%0" + OTP_LENGTH + "d", otp);
    }

    private void sendOtpEmail(String email, String otp, String documentTitle) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("OTP for Document Signing");
        message.setText(
                "Your OTP for signing the document \"" + documentTitle + "\" is:\n\n"
                        + otp
                        + "\n\nThis OTP is valid for "
                        + OTP_EXPIRY_MINUTES
                        + " minutes."
        );
        mailSender.send(message);
    }
}
