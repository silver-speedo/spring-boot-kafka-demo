package org.example.producerservice.service;

import static org.junit.jupiter.api.Assertions.*;

import org.example.producerservice.dto.RateLimitResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateLimiterServiceTest {

  private RateLimiterService rateLimiterService;

  @BeforeEach
  void setUp() {
    rateLimiterService = new RateLimiterService(5);
  }

  @Test
  @DisplayName("Should allow request within limit")
  void shouldAllowRequestWithinLimit() {
    RateLimitResult result = rateLimiterService.checkRateLimit("client-1");

    assertTrue(result.allowed());
    assertEquals(0, result.retryAfterSeconds());
  }

  @Test
  @DisplayName("Should eventually block requests beyond limit")
  void shouldEventuallyBlockRequestsBeyondLimit() {
    String clientId = "client-2";

    for (int i = 0; i < 5; i++) {
      RateLimitResult result = rateLimiterService.checkRateLimit(clientId);
      assertTrue(result.allowed(), "Request " + i + " should be allowed");
    }

    RateLimitResult blocked = rateLimiterService.checkRateLimit(clientId);
    assertFalse(blocked.allowed());
    assertTrue(blocked.retryAfterSeconds() > 0);
  }

  @Test
  @DisplayName("Should maintina separate buckets per client")
  void shouldMaintainSeparateBucketsPerClient() {
    String client1 = "client-1";
    String client2 = "client-2";

    for (int i = 0; i < 5; i++) {
      assertTrue(rateLimiterService.checkRateLimit(client1).allowed());
      assertTrue(rateLimiterService.checkRateLimit(client2).allowed());
    }

    assertFalse(rateLimiterService.checkRateLimit(client1).allowed());
    assertFalse(rateLimiterService.checkRateLimit(client2).allowed());
  }
}
