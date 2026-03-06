package it.portus.smartorder.ms.orderservice.business.stream.publisher;

import it.portus.smartorder.ms.orderservice.business.domain.events.EventStatus;
import it.portus.smartorder.ms.orderservice.business.domain.repositories.OrderOutboxRepository;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPollingPublisher {

  private final OrderOutboxRepository outboxRepository;
  private final OutboxSender outboxSender;

  private ScheduledExecutorService pollingExecutor;

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

  public void pollAndSend() {
    try {
      outboxRepository.findByStatus(EventStatus.PENDING).forEach(outboxSender::sendAsync);
    } catch (Exception e) {
      log.error("Error during outbox polling", e);
    }
  }
}
