package org.example.producerservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.example.producerservice.dto.RateLimitResult;
import org.example.producerservice.dto.ResultsResponse;
import org.example.producerservice.producer.KafkaOrderProducer;
import org.example.producerservice.service.RateLimiterService;
import org.example.shared.dto.CanonicalOrder;
import org.example.shared.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/orders")
@Validated
public class OrderController {

  private final KafkaOrderProducer producer;
  private final RateLimiterService rateLimiter;
  private final long limitPerMinute;

  public OrderController(
      KafkaOrderProducer producer,
      RateLimiterService rateLimiter,
      @Value("${rate.limit.per.minute}") long limitPerMinute) {
    this.producer = producer;
    this.rateLimiter = rateLimiter;
    this.limitPerMinute = limitPerMinute;
  }

  @Operation(summary = "Post multiple orders to Kafka for downstream processing")
  @PostMapping
  public ResponseEntity<ResultsResponse> publishRecords(
      @RequestBody List<@Valid CanonicalOrder> records, HttpServletRequest request) {
    RateLimitResult result = rateLimiter.checkRateLimit(request.getRemoteAddr());

    if (!result.allowed()) {
      throw new TooManyRequestsException(
          "Rate limit exceeded. You are limited to %s per minute".formatted(limitPerMinute),
          result.retryAfterSeconds());
    }

    List<String> failedOrderIds = Collections.synchronizedList(new ArrayList<>());
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);

    List<CompletableFuture<Void>> futures =
        records.stream()
            .map(
                order ->
                    producer
                        .sendAsync(order)
                        .thenAccept(
                            success -> {
                              if (success) {
                                successCount.incrementAndGet();
                              } else {
                                failureCount.incrementAndGet();
                                failedOrderIds.add(order.orderId());
                              }
                            }))
            .toList();

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    return ResponseEntity.ok(
        new ResultsResponse(
            records.size(), successCount.get(), failureCount.get(), failedOrderIds));
  }
}
