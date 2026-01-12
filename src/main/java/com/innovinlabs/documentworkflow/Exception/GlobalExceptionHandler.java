package com.innovinlabs.documentworkflow.Exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(409).body(ex.getMessage());
    }
    @ExceptionHandler(JobExecutionException.class)
    public ResponseEntity<JobExecutionException> handleJobExecutionException(JobExecutionException ex) {
        return ResponseEntity.status(500).body(ex);
    }
    @ExceptionHandler(SerializationException.class)
    public ResponseEntity<SerializationException> handleSerializationException(SerializationException ex) {
        return ResponseEntity.status(500).body(ex);
    }
}

