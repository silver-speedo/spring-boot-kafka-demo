package org.example.producerservice.producer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.shared.dto.CanonicalOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;

class KafkaOrderProducerTest {

  @SuppressWarnings("unchecked")
  KafkaTemplate<String, CanonicalOrder> kafkaTemplate =
      (KafkaTemplate<String, CanonicalOrder>) mock(KafkaTemplate.class);

  private KafkaOrderProducer producer;

  @BeforeEach
  void setUp() {
    MDC.put("correlationId", UUID.randomUUID().toString());
    Executor executor = Executors.newFixedThreadPool(4);

    producer = new KafkaOrderProducer("orders-topic", kafkaTemplate, executor);
  }

  @Test
  @DisplayName("Sending async should return true when Kafka send succeeds")
  void sendAsyncShouldReturnTrueWhenKafkaSendSucceeds() throws Exception {
    CanonicalOrder order =
        new CanonicalOrder("ORD-001", "CUST-001", Instant.now(), "PENDING", List.of(), null, 1);

    var record = new ProducerRecord<>("orders-topic", order.orderId(), order);
    record
        .headers()
        .add("correlation-id", MDC.get("correlationId").getBytes(StandardCharsets.UTF_8));

    when(kafkaTemplate.send(record)).thenReturn(CompletableFuture.completedFuture(null));

    CompletableFuture<Boolean> future = producer.sendAsync(order);

    assertTrue(future.join());
    verify(kafkaTemplate, atLeastOnce()).send(record);
  }

  @Test
  @DisplayName("Send async should return false when Kafka send fails")
  void sendAsyncShouldRetryAndFailWhenKafkaSendFails() throws Exception {
    CanonicalOrder order =
        new CanonicalOrder("ORD-002", "CUST-002", Instant.now(), "PENDING", List.of(), null, 1);

    var record = new ProducerRecord<>("orders-topic", order.orderId(), order);
    record
        .headers()
        .add("correlation-id", MDC.get("correlationId").getBytes(StandardCharsets.UTF_8));

    when(kafkaTemplate.send(record)).thenThrow(new RuntimeException("Simulated Kafka failure"));

    CompletableFuture<Boolean> future = producer.sendAsync(order);

    assertFalse(future.join());
    verify(kafkaTemplate, atLeast(4)).send(record);
  }

  @Test
  @DisplayName("Send async should respect the retry limit")
  void sendAsyncShouldRespectRetries() {
    CanonicalOrder order =
        new CanonicalOrder("ORD-003", "CUST-003", Instant.now(), "PENDING", List.of(), null, 1);

    var record = new ProducerRecord<>("orders-topic", order.orderId(), order);
    record
        .headers()
        .add("correlation-id", MDC.get("correlationId").getBytes(StandardCharsets.UTF_8));

    when(kafkaTemplate.send(record))
        .thenThrow(new RuntimeException("Fail 1"))
        .thenThrow(new RuntimeException("Fail 2"))
        .thenReturn(CompletableFuture.completedFuture(null));

    CompletableFuture<Boolean> future = producer.sendAsync(order);

    assertTrue(future.join());
    verify(kafkaTemplate, atLeast(3)).send(record);
  }
}
