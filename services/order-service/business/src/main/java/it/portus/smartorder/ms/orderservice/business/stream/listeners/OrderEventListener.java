package it.portus.smartorder.ms.orderservice.business.stream.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.portus.smartorder.events.OrderCreatedEvent;
import it.portus.smartorder.ms.orderservice.business.domain.events.EventStatus;
import it.portus.smartorder.ms.orderservice.business.domain.events.OrderOutboxEvent;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderState;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import it.portus.smartorder.ms.orderservice.business.domain.repositories.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener extends AbstractMongoEventListener<Order> {

  private final ObjectMapper objectMapper;
  private final OrderOutboxRepository outboxRepository;

  @SneakyThrows
  @Override
  public void onAfterSave(AfterSaveEvent<Order> event) {
    Order order = event.getSource();

    OrderState orderState = order.getState();
    if (orderState != null) {
      if (OrderStatus.PENDING.equals(orderState.getStatus())) {
        OrderCreatedEvent cloudEvent = new OrderCreatedEvent();
        cloudEvent.setOrderId(order.getId().toString());

        outboxRepository.save(
            OrderOutboxEvent.builder()
                .aggregateId(order.getId())
                .eventType(OrderCreatedEvent.class.getName())
                .payload(objectMapper.writeValueAsString(cloudEvent))
                .status(EventStatus.PENDING)
                .build());
      }
    } else {
      log.error("Order state is null for order {}", order.getId());
    }
  }
}
