package org.example.producerservice.producer;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.shared.dto.CanonicalOrder;
import org.example.shared.util.ExceptionLogUtil;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaOrderProducer {
  private final String topicName;
  private final Executor executor;
  private final KafkaTemplate<String, CanonicalOrder> kafkaTemplate;

  private byte[] correlationId;

  public KafkaOrderProducer(
      @Value("${kafka.topic.name}") String topicName,
      KafkaTemplate<String, CanonicalOrder> kafkaTemplate,
      @Qualifier("orderProducerExecutor") Executor executor) {
    this.topicName = topicName;
    this.kafkaTemplate = kafkaTemplate;
    this.executor = executor;
  }

  public CompletableFuture<Boolean> sendAsync(CanonicalOrder order) {
    Map<String, String> contextMap = MDC.getCopyOfContextMap();

    Supplier<Boolean> supplier =
        () -> {
          if (contextMap != null) {
            MDC.setContextMap(contextMap);
            correlationId = MDC.get("correlationId").getBytes(StandardCharsets.UTF_8);
          }

          try {
            // Simulate Kafka error to showcase retry functionality
            // Would not be in a production ready producer!
            if (Objects.equals(order.status(), "FAILED")) {
              throw new Exception("Kafka Exception - Retrying!");
            }

            var record = new ProducerRecord<>(topicName, order.orderId(), order);
            record.headers().add("correlation-id", correlationId);

            kafkaTemplate.send(record).get();

            log.info("[{}] Sent order: {}", Thread.currentThread().getName(), order.orderId());

            return true;
          } catch (Exception e) {
            ExceptionLogUtil.error(log, e, Thread.currentThread().getName());

            throw new CompletionException(e);
          }
        };

    CompletableFuture<Boolean> r = CompletableFuture.supplyAsync(supplier, executor);

    for (int i = 0; i < 3; i++) {
      r =
          r.exceptionallyComposeAsync(
              ex -> {
                long delayMillis = 300L;

                return CompletableFuture.supplyAsync(
                    supplier,
                    CompletableFuture.delayedExecutor(
                        delayMillis, TimeUnit.MILLISECONDS, executor));
              },
              executor);
    }

    r =
        r.exceptionally(
            ex -> {
              log.error(
                  "[{}] Send Order Failed: {}", Thread.currentThread().getName(), order.orderId());

              return false;
            });

    return r;
  }
}
