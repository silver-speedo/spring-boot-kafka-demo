package org.example.consumerservice.consumer;

import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.example.consumerservice.service.OrderPersistenceService;
import org.example.shared.dto.CanonicalOrder;
import org.example.shared.util.ExceptionLogUtil;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaOrderConsumer {

  private final OrderPersistenceService persistenceService;

  public KafkaOrderConsumer(OrderPersistenceService persistenceService) {
    this.persistenceService = persistenceService;
  }

  @KafkaListener(
      topics = "${kafka.topic.name}",
      groupId = "${spring.kafka.consumer.group-id}",
      containerFactory = "kafkaListenerContainerFactory")
  public void listen(
      CanonicalOrder order,
      Acknowledgment ack,
      @Header(value = "correlation-id", required = false) String correlationId) {
    try {
      MDC.put(
          "correlationId", correlationId != null ? correlationId : UUID.randomUUID().toString());

      log.info("[{}] Received order: {}", Thread.currentThread().getName(), order.orderId());

      boolean inserted = persistenceService.persistIfNotExists(order);

      if (inserted) {
        log.info("[{}] Order {} saved", Thread.currentThread().getName(), order.orderId());
      } else {
        log.info(
            "[{}] Order {} already exists, skipping",
            Thread.currentThread().getName(),
            order.orderId());
      }

      ack.acknowledge();
    } catch (Exception e) {
      ExceptionLogUtil.error(log, e, Thread.currentThread().getName());

      ack.nack(Duration.ofMillis(500L));
    }
  }
}
