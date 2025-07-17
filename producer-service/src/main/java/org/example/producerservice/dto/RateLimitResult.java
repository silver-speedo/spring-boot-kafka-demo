package org.example.producerservice.dto;

public record RateLimitResult(boolean allowed, long retryAfterSeconds) {}
