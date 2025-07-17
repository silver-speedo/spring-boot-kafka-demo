package org.example.producerservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Kafka Producer Demo")
                .version(getClass().getPackage().getImplementationVersion())
                .description("Kafka producer API to ingest test order messages."));
  }
}
