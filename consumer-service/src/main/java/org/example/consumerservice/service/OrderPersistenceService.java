package org.example.consumerservice.service;

import jakarta.transaction.Transactional;
import java.util.Optional;
import org.example.consumerservice.mapper.CanonicalOrderMapper;
import org.example.consumerservice.model.entity.CustomerEntity;
import org.example.consumerservice.model.entity.OrderEntity;
import org.example.consumerservice.repository.CustomerRepository;
import org.example.consumerservice.repository.OrderRepository;
import org.example.shared.dto.CanonicalOrder;
import org.springframework.stereotype.Service;

@Service
public class OrderPersistenceService {

  private final OrderRepository orderRepository;
  private final CanonicalOrderMapper canonicalOrderMapper;
  private final CustomerRepository customerRepository;

  public OrderPersistenceService(
      OrderRepository orderRepository,
      CanonicalOrderMapper canonicalOrderMapper,
      CustomerRepository customerRepository) {
    this.orderRepository = orderRepository;
    this.canonicalOrderMapper = canonicalOrderMapper;
    this.customerRepository = customerRepository;
  }

  @Transactional()
  public boolean persistIfNotExists(CanonicalOrder dto) {
    Optional<OrderEntity> existingOrder = orderRepository.findByOrderId(dto.orderId());

    if (existingOrder.isPresent()) {
      return false;
    }

    CustomerEntity customer =
        customerRepository
            .findByCustomerId(dto.customerId())
            .orElseGet(
                () -> {
                  CustomerEntity newCustomer = canonicalOrderMapper.mapToCustomerEntity(dto);

                  return customerRepository.save(newCustomer);
                });

    OrderEntity orderEntity = canonicalOrderMapper.mapToOrderEntity(dto, customer);
    orderRepository.save(orderEntity);

    return true;
  }
}
