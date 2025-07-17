package org.example.consumerservice.repository;

import java.util.Optional;
import java.util.UUID;
import org.example.consumerservice.model.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {

  Optional<CustomerEntity> findByCustomerId(String customerId);
}
