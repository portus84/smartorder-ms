package it.portus.smartorder.ms.orderservice.business.stream.listeners;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.portus.smartorder.events.OrderCreatedEvent;
import it.portus.smartorder.ms.orderservice.business.domain.events.EventStatus;
import it.portus.smartorder.ms.orderservice.business.domain.events.OrderOutboxEvent;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderState;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import it.portus.smartorder.ms.orderservice.business.domain.repositories.OrderOutboxRepository;
import java.util.UUID;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

  @Mock private ObjectMapper objectMapper;
  @Mock private OrderOutboxRepository outboxRepository;

  private OrderEventListener listener;

  @BeforeEach
  void setUp() {
    listener = new OrderEventListener(objectMapper, outboxRepository);
  }

  @Test
  void onAfterSave_OrderPending_SavesOutboxEvent() throws Exception {
    UUID orderId = UUID.randomUUID();

    Order order =
        Order.builder()
            .id(orderId)
            .state(OrderState.builder().status(OrderStatus.PENDING).build())
            .build();

    AfterSaveEvent<Order> event = new AfterSaveEvent<>(order, new Document(), "order");

    OrderCreatedEvent cloudEvent = new OrderCreatedEvent();
    cloudEvent.setOrderId(orderId.toString());

    when(objectMapper.writeValueAsString(cloudEvent))
        .thenReturn("{\"orderId\":\"" + orderId + "\"}");

    listener.onAfterSave(event);

    ArgumentCaptor<OrderOutboxEvent> captor = ArgumentCaptor.forClass(OrderOutboxEvent.class);
    verify(outboxRepository).save(captor.capture());

    OrderOutboxEvent saved = captor.getValue();
    assertEquals(orderId, saved.getAggregateId());
    assertEquals(OrderCreatedEvent.class.getName(), saved.getEventType());
    assertEquals("{\"orderId\":\"" + orderId + "\"}", saved.getPayload());
    assertEquals(EventStatus.PENDING, saved.getStatus());
  }

  @Test
  void onAfterSave_OrderWithEmptyState_DoesNotSaveOutboxEvent() {
    UUID orderId = UUID.randomUUID();

    Order order =
        Order.builder().id(orderId).state(OrderState.builder().status(null).build()).build();

    AfterSaveEvent<Order> event = new AfterSaveEvent<>(order, new Document(), "order");

    listener.onAfterSave(event);

    verifyNoInteractions(outboxRepository);
  }
}
