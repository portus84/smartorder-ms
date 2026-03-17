package it.portus.smartorder.ms.orderservice.business.stream.adapter.impl;

import it.portus.smartorder.events.OrderConfirmedEvent;
import it.portus.smartorder.events.OrderOutOfStockEvent;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderState;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import it.portus.smartorder.ms.orderservice.business.services.OrderService;
import it.portus.smartorder.ms.orderservice.business.stream.adapter.OrderEventAdapter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventAdapterImpl implements OrderEventAdapter {

  private final OrderService orderService;

  @Override
  public void onOrderConfirmed(OrderConfirmedEvent event) {
    handleOrderEvent(event.getOrderId(), OrderStatus.CONFIRMED, null);
  }

  @Override
  public void onOrderOutOfStock(OrderOutOfStockEvent event) {
    handleOrderEvent(event.getOrderId(), OrderStatus.OUT_OF_STOCK, event.getReason());
  }

  private void handleOrderEvent(String orderId, OrderStatus status, String reason) {
    if (OrderStatus.CONFIRMED.equals(status)) {
      log.info("Order {} confirmed, updating status to: {}", orderId, status);
    } else {
      log.warn("Order {} Out Of Stock: {}, updating status: {}", orderId, reason, status);
    }

    orderService
        .findById(UUID.fromString(orderId))
        .ifPresentOrElse(
            order -> {
              order.setState(OrderState.builder().status(status).reason(reason).build());
              orderService.update(order.getId(), order);
            },
            () -> log.error("Order {} not found", orderId));
  }
}
