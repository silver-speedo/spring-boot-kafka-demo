package org.example.producerservice.dto;

import java.util.List;

public record ResultsResponse(
    int processed, int successful, int failed, List<String> failedOrderIds) {}
