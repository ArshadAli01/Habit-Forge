package com.habittracker.common.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * STANDARD ERROR RESPONSE FORMAT
 *
 * Used across all modules.
 */
@Getter
@Builder
public class ErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}