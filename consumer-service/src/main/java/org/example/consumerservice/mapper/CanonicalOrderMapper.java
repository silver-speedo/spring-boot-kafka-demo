package org.example.consumerservice.mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.example.consumerservice.model.entity.CustomerEntity;
import org.example.consumerservice.model.entity.OrderEntity;
import org.example.consumerservice.model.entity.OrderItemEntity;
import org.example.shared.dto.CanonicalOrder;
import org.springframework.stereotype.Component;

@Component
public class CanonicalOrderMapper {

  public OrderEntity mapToOrderEntity(CanonicalOrder dto, CustomerEntity customer) {
    UUID orderUuid = UUID.randomUUID();

    List<OrderItemEntity> items =
        dto.items().stream()
            .map(
                item ->
                    OrderItemEntity.builder()
                        .id(UUID.randomUUID())
                        .order(null)
                        .sku(item.sku())
                        .name(item.name())
                        .quantity(item.quantity())
                        .unitPrice(item.unitPrice())
                        .build())
            .collect(Collectors.toList());

    OrderEntity order =
        OrderEntity.builder()
            .id(orderUuid)
            .orderId(dto.orderId())
            .customer(customer)
            .orderTimestamp(dto.orderTimestamp())
            .status(dto.status())
            .version(dto.version())
            .items(items)
            .build();

    items.forEach(item -> item.setOrder(order));

    return order;
  }

  public CustomerEntity mapToCustomerEntity(CanonicalOrder dto) {
    return CustomerEntity.builder()
        .id(UUID.randomUUID())
        .customerId(dto.customerId())
        .name("Customer " + dto.customerId())
        .street(dto.shippingAddress().street())
        .city(dto.shippingAddress().city())
        .postalCode(dto.shippingAddress().postalCode())
        .country(dto.shippingAddress().country())
        .build();
  }
}
