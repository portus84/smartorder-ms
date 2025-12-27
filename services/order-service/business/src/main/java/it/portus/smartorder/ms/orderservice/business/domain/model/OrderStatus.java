package it.portus.smartorder.ms.orderservice.business.domain.model;

public enum OrderStatus {
  PENDING,
  OUT_OF_STOCK,
  CONFIRMED,
  SHIPPED,
  DELIVERED,
  CANCELLED
}
