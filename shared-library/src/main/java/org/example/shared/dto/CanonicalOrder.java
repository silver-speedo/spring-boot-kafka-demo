package org.example.shared.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.List;

public record CanonicalOrder(
    @NotBlank(message = "Order ID must not be blank") String orderId,
    @NotBlank(message = "Customer ID must not be blank") String customerId,
    @NotNull(message = "Order timestamp is required")
        @PastOrPresent(message = "Order timestamp cannot be in the future")
        Instant orderTimestamp,
    @NotBlank(message = "Status must not be blank")
        @Pattern(
            regexp = "PENDING|PROCESSING|SHIPPED|CANCELLED|FAILED",
            message = "Status must be one of: PENDING, PROCESSING, SHIPPED, CANCELLED")
        String status,
    @NotEmpty(message = "At least one order item is required") @Valid List<@Valid OrderItem> items,
    @NotNull(message = "Shipping address is required") @Valid ShippingAddress shippingAddress,
    @Min(value = 1, message = "Version must be at least 1") int version) {
  public record OrderItem(
      @NotBlank(message = "SKU must not be blank") String sku,
      @NotBlank(message = "Name must not be blank") String name,
      @Min(value = 1, message = "Quantity must be at least 1") int quantity,
      @DecimalMin(value = "0.01", inclusive = true, message = "Unit price must be at least 0.01")
          double unitPrice) {}

  public record ShippingAddress(
      @NotBlank(message = "Street is required") String street,
      @NotBlank(message = "City is required") String city,
      @NotBlank(message = "Postal code is required") String postalCode,
      @NotBlank(message = "Country is required") String country) {}
}
