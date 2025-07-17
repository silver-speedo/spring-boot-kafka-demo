package org.example.consumerservice.consumer;

import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.example.consumerservice.service.OrderPersistenceService;
import org.example.shared.dto.CanonicalOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

class KafkaOrderConsumerTest {

  private OrderPersistenceService persistenceService;
  private Acknowledgment acknowledgment;
  private KafkaOrderConsumer listener;

  @BeforeEach
  void setUp() {
    persistenceService = mock(OrderPersistenceService.class);
    acknowledgment = mock(Acknowledgment.class);
    listener = new KafkaOrderConsumer(persistenceService);
  }

  @Test
  @DisplayName("Should persist a new order to the database and acknowledge")
  void shouldPersistNewOrderAndAcknowledge() {
    CanonicalOrder order =
        new CanonicalOrder("ORD-001", "CUST-001", Instant.now(), "PENDING", List.of(), null, 1);

    when(persistenceService.persistIfNotExists(order)).thenReturn(true);

    listener.listen(order, acknowledgment, "corr-id-abc");

    verify(persistenceService).persistIfNotExists(order);
    verify(acknowledgment).acknowledge();
    verifyNoMoreInteractions(acknowledgment);
  }

  @Test
  @DisplayName("Should skip duplicate orders and acknowledge")
  void shouldSkipDuplicateOrderAndAcknowledge() {
    CanonicalOrder order =
        new CanonicalOrder("ORD-002", "CUST-002", Instant.now(), "PENDING", List.of(), null, 1);

    when(persistenceService.persistIfNotExists(order)).thenReturn(false);

    listener.listen(order, acknowledgment, null);

    verify(persistenceService).persistIfNotExists(order);
    verify(acknowledgment).acknowledge();
  }

  @Test
  @DisplayName("Should handle exceptions and nack")
  void shouldHandleExceptionAndNack() {
    CanonicalOrder order =
        new CanonicalOrder("ORD-003", "CUST-003", Instant.now(), "PENDING", List.of(), null, 1);

    when(persistenceService.persistIfNotExists(order)).thenThrow(new RuntimeException("Boom"));

    listener.listen(order, acknowledgment, "corr-id-explosion");

    verify(acknowledgment, never()).acknowledge();
    verify(acknowledgment).nack(any(Duration.class));
  }
}
