package it.portus.smartorder.ms.orderservice.business.stream.config;

import it.portus.smartorder.events.OrderConfirmedEvent;
import it.portus.smartorder.events.OrderOutOfStockEvent;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderState;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import it.portus.smartorder.ms.orderservice.business.services.OrderService;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderAvailabilityListener {

  private final OrderService orderService;

  @Bean
  public Consumer<OrderConfirmedEvent> orderConfirmedConsumer() {
    return event -> handleOrderEvent(event.getOrderId(), OrderStatus.CONFIRMED, null);
  }

  @Bean
  public Consumer<OrderOutOfStockEvent> orderOutOfStockConsumer() {
    return event ->
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
