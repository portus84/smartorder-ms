package it.portus.smartorder.ms.orderservice.business.stream.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.portus.business.commons.stream.publisher.EventPublisher;
import it.portus.smartorder.ms.orderservice.business.domain.events.EventStatus;
import it.portus.smartorder.ms.orderservice.business.domain.events.OrderOutboxEvent;
import it.portus.smartorder.ms.orderservice.business.domain.repositories.OrderOutboxRepository;
import it.portus.smartorder.ms.orderservice.business.stream.BindingNames;
import java.util.concurrent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher implements EventPublisher<OrderOutboxEvent> {

  private final OrderOutboxRepository outboxRepository;
  private final StreamBridge streamBridge;
  private final ObjectMapper objectMapper;

  private final Executor taskScheduler;

  @Override
  public void publish(OrderOutboxEvent outboxEvent) {
    CompletableFuture.runAsync(() -> publishEvent(outboxEvent), taskScheduler);
  }

  private void publishEvent(OrderOutboxEvent event) {
    try {
      if (!outboxRepository.existsById(event.getId())) {
        log.error("Outbox event {} does not exist in DB, skipping", event.getId());
        return;
      }

      Class<?> eventClass = Class.forName(event.getEventType());
      Object eventPayload = objectMapper.readValue(event.getPayload(), eventClass);

      boolean sent = streamBridge.send(BindingNames.PUBLISH_ORDER_CREATED, eventPayload);

      event.setStatus(sent ? EventStatus.SENT : EventStatus.FAILED);
      log.debug("Outbox event {} -> {}", event.getId(), event.getStatus());

      outboxRepository.save(event);
    } catch (Exception e) {
      log.error("Error publishing outbox event {}", event.getId(), e);
      event.setStatus(EventStatus.FAILED);
      outboxRepository.save(event);
    }
  }
}
