package it.portus.smartorder.ms.orderservice.business.stream.publisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.portus.smartorder.ms.orderservice.business.domain.events.EventStatus;
import it.portus.smartorder.ms.orderservice.business.domain.events.OrderOutboxEvent;
import it.portus.smartorder.ms.orderservice.business.domain.repositories.OrderOutboxRepository;
import it.portus.smartorder.ms.orderservice.business.stream.BindingNames;
import java.util.List;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

  @Mock private OrderOutboxRepository outboxRepository;

  @Mock private StreamBridge streamBridge;

  @Mock private ObjectMapper objectMapper;

  @InjectMocks private OutboxPublisher outboxPublisher;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(outboxPublisher, "mongoTemplate", mock(MongoTemplate.class));
  }

  @Test
  void processPendingEvents_PendingEventsExist_ShouldSubmitTasks() {
    List<OrderOutboxEvent> events =
        Instancio.ofList(OrderOutboxEvent.class)
            .size(3)
            .set(Select.field(OrderOutboxEvent::getEventType), String.class.getName())
            .set(Select.field(OrderOutboxEvent::getStatus), EventStatus.PENDING)
            .create();

    when(outboxRepository.findByStatus(EventStatus.PENDING)).thenReturn(events);

    outboxPublisher.start();

    verify(outboxRepository, times(1)).findByStatus(EventStatus.PENDING);
  }

  @Test
  void sendEvent_EventSentSuccessfully_ShouldSetStatusSent() throws Exception {
    OrderOutboxEvent event = Instancio.create(OrderOutboxEvent.class);
    event.setStatus(EventStatus.PENDING);
    event.setEventType(String.class.getName());

    when(objectMapper.readValue(event.getPayload(), String.class)).thenReturn(event.getPayload());
    when(streamBridge.send(BindingNames.PUBLISH_ORDER_CREATED, event.getPayload()))
        .thenReturn(true);

    ReflectionTestUtils.invokeMethod(outboxPublisher, "sendEvent", event);

    assertEquals(EventStatus.SENT, event.getStatus());
    verify(outboxRepository).save(event);
  }

  @Test
  void sendEvent_SendFails_ShouldSetStatusFailed() throws Exception {
    OrderOutboxEvent event = Instancio.create(OrderOutboxEvent.class);
    event.setStatus(EventStatus.PENDING);
    event.setEventType(String.class.getName());

    when(objectMapper.readValue(event.getPayload(), String.class)).thenReturn(event.getPayload());
    when(streamBridge.send(any(), any())).thenReturn(false);

    ReflectionTestUtils.invokeMethod(outboxPublisher, "sendEvent", event);

    assertEquals(EventStatus.FAILED, event.getStatus());
    verify(outboxRepository).save(event);
  }

  @Test
  void sendEvent_ExceptionThrown_ShouldSetStatusFailed() throws Exception {
    OrderOutboxEvent event = Instancio.create(OrderOutboxEvent.class);
    event.setStatus(EventStatus.PENDING);
    event.setEventType(String.class.getName());

    when(objectMapper.readValue(event.getPayload(), String.class))
        .thenThrow(new RuntimeException("boom"));

    ReflectionTestUtils.invokeMethod(outboxPublisher, "sendEvent", event);

    assertEquals(EventStatus.FAILED, event.getStatus());
    verify(outboxRepository).save(event);
  }
}
