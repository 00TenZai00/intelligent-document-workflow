package com.innovinlabs.documentworkflow.service;

import com.innovinlabs.documentworkflow.service.dto.SignatureJobPayload;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void sendSignatureRequest(SignatureJobPayload payload) {
        System.out.println("Sending signature request: " + payload);
    }

    public void sendReminder(SignatureJobPayload payload) {
        System.out.println("Sending reminder: " + payload);
    }

    public void sendConfirmation(SignatureJobPayload payload) {
        System.out.println("Sending confirmation: " + payload);
    }
}
