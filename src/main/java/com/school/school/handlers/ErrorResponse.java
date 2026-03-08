package com.school.school.handlers;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;

@Getter
public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final Map<String, ?> details;

    public ErrorResponse(
                         int status,
                         String error,
                         String message,
                         String path,
                         Map<String, ?> details) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.details = details;
    }
}
