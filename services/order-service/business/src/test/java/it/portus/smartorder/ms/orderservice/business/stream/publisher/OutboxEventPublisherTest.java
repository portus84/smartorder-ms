package it.portus.smartorder.ms.orderservice.business.stream.publisher;

import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.portus.business.commons.stream.publisher.EventPublisher;
import it.portus.smartorder.events.OrderCreatedEvent;
import it.portus.smartorder.ms.orderservice.business.domain.events.EventStatus;
import it.portus.smartorder.ms.orderservice.business.domain.events.OrderOutboxEvent;
import it.portus.smartorder.ms.orderservice.business.domain.repositories.OrderOutboxRepository;
import it.portus.smartorder.ms.orderservice.business.stream.BindingNames;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherTest {

  @Mock private OrderOutboxRepository outboxRepository;
  @Mock private StreamBridge streamBridge;
  @Mock private ObjectMapper objectMapper;

  private EventPublisher<OrderOutboxEvent> publisher;

  @BeforeEach
  void setUp() {
    publisher =
        new OutboxEventPublisher(outboxRepository, streamBridge, objectMapper, Runnable::run);
  }

  @Test
  void publish_EventExistsAndStreamSendSucceeds_StatusSetToSent() throws Exception {
    OrderOutboxEvent event = new OrderOutboxEvent();
    UUID eventId = event.getId();

    event.setEventType(OrderCreatedEvent.class.getName());
    event.setPayload("{}");

    OrderCreatedEvent payload = new OrderCreatedEvent();

    when(outboxRepository.existsById(eventId)).thenReturn(true);
    when(objectMapper.readValue("{}", OrderCreatedEvent.class)).thenReturn(payload);
    when(streamBridge.send(BindingNames.PUBLISH_ORDER_CREATED, payload)).thenReturn(true);

    publisher.publish(event);

    verify(streamBridge).send(BindingNames.PUBLISH_ORDER_CREATED, payload);

    verify(outboxRepository).save(argThat(e -> e.getStatus() == EventStatus.SENT));
  }

  @Test
  void publish_EventExistsAndStreamSendFails_StatusSetToFailed() throws Exception {
    OrderOutboxEvent event = new OrderOutboxEvent();
    UUID eventId = event.getId();

    event.setEventType(OrderCreatedEvent.class.getName());
    event.setPayload("{}");

    OrderCreatedEvent payload = new OrderCreatedEvent();

    when(outboxRepository.existsById(eventId)).thenReturn(true);
    when(objectMapper.readValue("{}", OrderCreatedEvent.class)).thenReturn(payload);
    when(streamBridge.send(BindingNames.PUBLISH_ORDER_CREATED, payload)).thenReturn(false);

    publisher.publish(event);

    verify(outboxRepository).save(argThat(e -> e.getStatus() == EventStatus.FAILED));
  }

  @Test
  void publish_EventDoesNotExistInDatabase_NoEventIsPublished() {
    OrderOutboxEvent event = new OrderOutboxEvent();
    UUID eventId = event.getId();

    when(outboxRepository.existsById(eventId)).thenReturn(false);

    publisher.publish(event);

    verify(streamBridge, times(0)).send(anyString(), any());

    verify(outboxRepository, times(0)).save(any());
  }
}
