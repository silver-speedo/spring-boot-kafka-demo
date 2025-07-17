package org.example.producerservice.config;

import org.example.shared.dto.CanonicalOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

  @Bean
  public KafkaTemplate<String, CanonicalOrder> kafkaTemplate(
      ProducerFactory<String, CanonicalOrder> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }
}
