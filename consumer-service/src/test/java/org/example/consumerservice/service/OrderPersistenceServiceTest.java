package org.example.consumerservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.example.consumerservice.mapper.CanonicalOrderMapper;
import org.example.consumerservice.model.entity.CustomerEntity;
import org.example.consumerservice.model.entity.OrderEntity;
import org.example.consumerservice.repository.CustomerRepository;
import org.example.consumerservice.repository.OrderRepository;
import org.example.shared.dto.CanonicalOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderPersistenceServiceTest {

  private OrderRepository orderRepository;
  private CustomerRepository customerRepository;
  private CanonicalOrderMapper orderMapper;
  private OrderPersistenceService service;

  @BeforeEach
  void setUp() {
    orderRepository = mock(OrderRepository.class);
    customerRepository = mock(CustomerRepository.class);
    orderMapper = mock(CanonicalOrderMapper.class);
    
    service = new OrderPersistenceService(orderRepository, orderMapper, customerRepository);
  }

  @Test
  @DisplayName("Should return false if the order already exists in the database")
  void shouldReturnFalseIfOrderAlreadyExists() {
    CanonicalOrder order =
        new CanonicalOrder("ORD-001", "CUST-001", Instant.now(), "PENDING", List.of(), null, 1);

    when(orderRepository.findByOrderId(order.orderId()))
        .thenReturn(Optional.of(mock(OrderEntity.class)));

    boolean result = service.persistIfNotExists(order);

    assertFalse(result);
    verify(orderRepository, never()).save(any());
    verify(customerRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should save the order to the database when the customer exists")
  void shouldSaveOrderWhenCustomerExists() {
    CanonicalOrder order =
        new CanonicalOrder("ORD-002", "CUST-002", Instant.now(), "PENDING", List.of(), null, 1);

    CustomerEntity customer = mock(CustomerEntity.class);
    OrderEntity orderEntity = mock(OrderEntity.class);

    when(orderRepository.findByOrderId(order.orderId())).thenReturn(Optional.empty());
    when(customerRepository.findByCustomerId(order.customerId())).thenReturn(Optional.of(customer));
    when(orderMapper.mapToOrderEntity(order, customer)).thenReturn(orderEntity);

    boolean result = service.persistIfNotExists(order);

    assertTrue(result);
    verify(customerRepository, never()).save(any());
    verify(orderRepository).save(orderEntity);
  }

  @Test
  @DisplayName(
      "Should save the customer and the order to the database when the customer does not exist")
  void shouldSaveCustomerAndOrderWhenCustomerDoesNotExist() {
    CanonicalOrder order =
        new CanonicalOrder("ORD-003", "CUST-003", Instant.now(), "PENDING", List.of(), null, 1);

    CustomerEntity newCustomer = mock(CustomerEntity.class);
    OrderEntity orderEntity = mock(OrderEntity.class);

    when(orderRepository.findByOrderId(order.orderId())).thenReturn(Optional.empty());
    when(customerRepository.findByCustomerId(order.customerId())).thenReturn(Optional.empty());
    when(orderMapper.mapToCustomerEntity(order)).thenReturn(newCustomer);
    when(customerRepository.save(newCustomer)).thenReturn(newCustomer);
    when(orderMapper.mapToOrderEntity(order, newCustomer)).thenReturn(orderEntity);

    boolean result = service.persistIfNotExists(order);

    assertTrue(result);
    verify(customerRepository).save(newCustomer);
    verify(orderRepository).save(orderEntity);
  }
}
