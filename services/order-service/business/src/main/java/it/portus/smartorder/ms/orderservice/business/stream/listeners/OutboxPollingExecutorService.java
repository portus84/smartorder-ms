package it.portus.smartorder.ms.orderservice.business.stream.listeners;

import it.portus.business.commons.stream.publisher.EventPublisher;
import it.portus.smartorder.ms.orderservice.business.domain.events.EventStatus;
import it.portus.smartorder.ms.orderservice.business.domain.events.OrderOutboxEvent;
import it.portus.smartorder.ms.orderservice.business.domain.repositories.OrderOutboxRepository;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPollingExecutorService {

  private final OrderOutboxRepository outboxRepository;
  private final EventPublisher<OrderOutboxEvent> publisher;

  private ScheduledExecutorService pollingExecutor;

  public void pollAndSend() {
    try {
      outboxRepository.findByStatus(EventStatus.PENDING).forEach(publisher::publish);
    } catch (Exception e) {
      log.error("Error during outbox polling", e);
    }
  }

  public void start() {
    log.warn("Outbox polling STARTED");

    pollingExecutor =
        Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "OutboxPollingThread"));

    pollingExecutor.scheduleWithFixedDelay(this::pollAndSend, 0, 5, TimeUnit.SECONDS);
  }

  @PreDestroy
  public void shutdown() {
    if (pollingExecutor != null) {
      pollingExecutor.shutdown();
    }
  }
}
