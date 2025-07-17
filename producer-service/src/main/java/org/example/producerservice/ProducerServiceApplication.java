package org.example.producerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.example.producerservice", "org.example.shared"})
public class ProducerServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProducerServiceApplication.class, args);
  }
}
