package org.example.consumerservice.repository;

import java.util.Optional;
import java.util.UUID;
import org.example.consumerservice.model.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

  Optional<OrderEntity> findByOrderId(String orderId);
}
