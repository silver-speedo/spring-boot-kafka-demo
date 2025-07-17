package org.example.consumerservice.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.shared.dto.CanonicalOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  String bootstrapServers;

  @Value("${spring.kafka.consumer.group-id}")
  String consumerGroupId;

  @Value("${spring.kafka.consumer.auto-offset-reset}")
  String autoOffsetReset;

  @Value("${spring.kafka.consumer.enable-auto-commit}")
  String enableAutoCommit;

  @Bean
  public KafkaTemplate<String, CanonicalOrder> kafkaTemplate(
      ProducerFactory<String, CanonicalOrder> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }

  @Bean
  public ConsumerFactory<String, CanonicalOrder> consumerFactory() {
    JsonDeserializer<CanonicalOrder> deserializer = new JsonDeserializer<>(CanonicalOrder.class);
    deserializer.setRemoveTypeHeaders(false);
    deserializer.addTrustedPackages("*");
    deserializer.setUseTypeMapperForKey(true);

    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);

    return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, CanonicalOrder>
      kafkaListenerContainerFactory(
          @Value("${kafka.consumer.count}") int consumerCount, DefaultErrorHandler errorHandler) {
    ConcurrentKafkaListenerContainerFactory<String, CanonicalOrder> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setConcurrency(consumerCount);
    factory.setBatchListener(false);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
    factory.setCommonErrorHandler(errorHandler);

    return factory;
  }

  @Bean
  public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
    FixedBackOff backOff = new FixedBackOff(1000L, 3);
    DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

    handler.addNotRetryableExceptions(IllegalArgumentException.class);

    return handler;
  }

  @Bean
  public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
      KafkaTemplate<String, CanonicalOrder> kafkaTemplate) {

    return new DeadLetterPublishingRecoverer(
        kafkaTemplate, (record, ex) -> new TopicPartition("orders-dlc", record.partition()));
  }
}
