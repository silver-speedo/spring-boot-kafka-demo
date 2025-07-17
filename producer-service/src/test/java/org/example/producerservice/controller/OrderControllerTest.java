package org.example.producerservice.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.example.producerservice.dto.RateLimitResult;
import org.example.producerservice.producer.KafkaOrderProducer;
import org.example.producerservice.service.RateLimiterService;
import org.example.shared.dto.CanonicalOrder;
import org.example.shared.exception.TooManyRequestsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderControllerTest {

  private KafkaOrderProducer producer;
  private RateLimiterService rateLimiter;
  private HttpServletRequest request;
  private OrderController controller;

  @BeforeEach
  void setUp() {
    producer = mock(KafkaOrderProducer.class);
    rateLimiter = mock(RateLimiterService.class);
    request = mock(HttpServletRequest.class);

    controller = new OrderController(producer, rateLimiter, 50L);

    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
  }

  @Test
  @DisplayName("Test publishing records that all succeed returns the correct result")
  void testPublishRecordsAllSuccess() {
    CanonicalOrder order1 =
        new CanonicalOrder("ORD-001", "CUST-001", Instant.now(), "NEW", List.of(), null, 1);
    CanonicalOrder order2 =
        new CanonicalOrder("ORD-002", "CUST-002", Instant.now(), "NEW", List.of(), null, 1);

    when(rateLimiter.checkRateLimit("127.0.0.1")).thenReturn(new RateLimitResult(true, 0));
    when(producer.sendAsync(order1)).thenReturn(CompletableFuture.completedFuture(true));
    when(producer.sendAsync(order2)).thenReturn(CompletableFuture.completedFuture(true));

    var response = controller.publishRecords(List.of(order1, order2), request);
    var body = response.getBody();

    assertNotNull(body);
    assertEquals(2, body.processed());
    assertEquals(2, body.successful());
    assertEquals(0, body.failed());
    assertTrue(body.failedOrderIds().isEmpty());
  }

  @Test
  @DisplayName("Test publishing records with some failures returns the correct result")
  void testPublishRecordsSomeFailures() {
    CanonicalOrder order1 =
        new CanonicalOrder("ORD-001", "CUST-001", Instant.now(), "NEW", List.of(), null, 1);
    CanonicalOrder order2 =
        new CanonicalOrder("ORD-002", "CUST-002", Instant.now(), "NEW", List.of(), null, 1);

    when(rateLimiter.checkRateLimit("127.0.0.1")).thenReturn(new RateLimitResult(true, 0));
    when(producer.sendAsync(order1)).thenReturn(CompletableFuture.completedFuture(true));
    when(producer.sendAsync(order2)).thenReturn(CompletableFuture.completedFuture(false));

    var response = controller.publishRecords(List.of(order1, order2), request);
    var body = response.getBody();

    assertNotNull(body);
    assertEquals(2, body.processed());
    assertEquals(1, body.successful());
    assertEquals(1, body.failed());
    assertEquals(List.of("ORD-002"), body.failedOrderIds());
  }

  @Test
  @DisplayName("Test rate limit exceeded works as intended")
  void testPublishRecordsRateLimitExceeded() {
    when(rateLimiter.checkRateLimit("127.0.0.1")).thenReturn(new RateLimitResult(false, 42));
    CanonicalOrder order =
        new CanonicalOrder("ORD-001", "CUST-001", Instant.now(), "NEW", List.of(), null, 1);

    TooManyRequestsException ex =
        assertThrows(
            TooManyRequestsException.class,
            () -> controller.publishRecords(List.of(order), request));

    assertEquals("42", ex.getRetryAfterSeconds());
  }
}
