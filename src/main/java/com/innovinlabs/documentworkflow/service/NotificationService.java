package com.innovinlabs.documentworkflow.service;

import com.innovinlabs.documentworkflow.service.dto.SignatureJobPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    public void sendSignatureRequest(SignatureJobPayload payload) {
        log.info("Sending signature request: {}", payload);
    }

    public void sendReminder(SignatureJobPayload payload) {
        log.info("Sending reminder: {}", payload);
    }

    public void sendConfirmation(SignatureJobPayload payload) {
        log.info("Sending confirmation: {}", payload);
    }
}
