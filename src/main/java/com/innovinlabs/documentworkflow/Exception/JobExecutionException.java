package com.innovinlabs.documentworkflow.Exception;

public class JobExecutionException extends RuntimeException {
    public JobExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}