package org.example.producerservice.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.example.shared.config.ThreadPoolProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class ThreadPoolConfig {

  @Bean
  public Executor orderProducerExecutor(ThreadPoolProperties props) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(props.getCorePoolSize());
    executor.setMaxPoolSize(props.getMaxPoolSize());
    executor.setQueueCapacity(props.getQueueCapacity());
    executor.setKeepAliveSeconds(props.getKeepAliveSeconds());
    executor.setThreadNamePrefix(props.getNamePrefix());
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
  }
}
