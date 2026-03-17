package it.portus.smartorder.ms.orderservice.business.stream.listeners;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventListener {

  private final ChangeMongoStreamListener mongoStreamListener;
  private final OutboxPollingExecutorService pollingExecutorService;

  private final ThreadPoolTaskExecutor outboxTaskExecutor;

  private Future<?> changeStreamTask;

  @EventListener(ApplicationReadyEvent.class)
  public void start() {
    log.info("OutboxPublisher STARTING after context ready");

    pollingExecutorService.pollAndSend();

    if (mongoStreamListener.isChangeStreamSupported()) {
      log.debug("Mongo Change Stream supported → starting listener");
      changeStreamTask = outboxTaskExecutor.submit(mongoStreamListener::listen);
    } else {
      log.warn("Mongo Change Stream NOT supported → falling back to polling");
      pollingExecutorService.start();
    }
  }

  @PreDestroy
  public void shutdown() {
    if (changeStreamTask != null) {
      changeStreamTask.cancel(true);
    }
  }
}
