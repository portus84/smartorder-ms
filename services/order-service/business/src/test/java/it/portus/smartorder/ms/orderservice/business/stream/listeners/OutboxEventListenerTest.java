package it.portus.smartorder.ms.orderservice.business.stream.listeners;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@ExtendWith(MockitoExtension.class)
class OutboxEventListenerTest {

  @Mock private ChangeMongoStreamListener mongoStreamListener;

  @Mock private OutboxPollingExecutorService pollingExecutorService;

  @Mock private ThreadPoolTaskExecutor outboxTaskExecutor;

  @Mock private Future<?> future;

  @InjectMocks private OutboxEventListener outboxEventListener;

  @Test
  void start_ChangeStreamSupported_SubmitsChangeStreamTask() {
    when(mongoStreamListener.isChangeStreamSupported()).thenReturn(true);

    doReturn(future).when(outboxTaskExecutor).submit(any(Runnable.class));

    outboxEventListener.start();

    verify(pollingExecutorService).pollAndSend();
    verify(outboxTaskExecutor).submit(any(Runnable.class));
    verify(pollingExecutorService, never()).start();
  }

  @Test
  void start_ChangeStreamNotSupported_StartsPollingFallback() {
    when(mongoStreamListener.isChangeStreamSupported()).thenReturn(false);

    outboxEventListener.start();

    verify(pollingExecutorService).pollAndSend();
    verify(pollingExecutorService).start();
    verify(outboxTaskExecutor, never()).submit(any(Runnable.class));
  }

  @Test
  void shutdown_ChangeStreamTaskRunning_CancelsFuture() {
    when(mongoStreamListener.isChangeStreamSupported()).thenReturn(true);

    doReturn(future).when(outboxTaskExecutor).submit(any(Runnable.class));

    outboxEventListener.start();

    outboxEventListener.shutdown();

    verify(future).cancel(true);
  }

  @Test
  void shutdown_NoChangeStreamTask_DoesNothing() {
    assertDoesNotThrow(() -> outboxEventListener.shutdown());
    verifyNoInteractions(outboxTaskExecutor);
  }
}
