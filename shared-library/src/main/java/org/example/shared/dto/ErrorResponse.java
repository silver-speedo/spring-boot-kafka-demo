package org.example.shared.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
    String error, Map<String, String> message, String path, int status, LocalDateTime timestamp) {}
