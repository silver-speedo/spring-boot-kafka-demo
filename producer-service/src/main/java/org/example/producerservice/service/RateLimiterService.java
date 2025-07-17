package org.example.producerservice.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.example.producerservice.dto.RateLimitResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RateLimiterService {

  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  private final long limitPerMinute;

  public RateLimiterService(@Value("${rate.limit.per.minute}") long limitPerMinute) {
    this.limitPerMinute = limitPerMinute;
  }

  private Bucket createBucket() {
    return Bucket.builder()
        .addLimit(
            Bandwidth.classic(
                limitPerMinute, Refill.intervally(limitPerMinute, Duration.ofMinutes(1))))
        .build();
  }

  public RateLimitResult checkRateLimit(String clientId) {
    Bucket bucket = buckets.computeIfAbsent(clientId, __ -> createBucket());
    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

    if (probe.isConsumed()) {
      return new RateLimitResult(true, 0);
    } else {
      return new RateLimitResult(
          false, Math.max(Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds(), 1));
    }
  }
}
