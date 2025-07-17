package org.example.producerservice.config;

import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

  @Bean
  public NewTopic canonicalTopic(
      @Value("${kafka.topic.name}") String topicName,
      @Value("${kafka.partition.count}") int partitionCount) {
    return new NewTopic(topicName, partitionCount, (short) 1)
        .configs(Map.of(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT));
  }
}
