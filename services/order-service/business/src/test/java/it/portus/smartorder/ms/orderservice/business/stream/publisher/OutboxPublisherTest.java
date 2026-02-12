package it.portus.smartorder.ms.orderservice.business.stream.publisher;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mongodb.client.MongoDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

  @Mock private MongoTemplate mongoTemplate;

  @Mock private OutboxPollingPublisher pollingPublisher;

  @Mock private ExecutorService executorService;

  @Mock private MongoDatabase mongoDatabase;

  @Mock private Future<?> future;

  @InjectMocks private OutboxPublisher outboxPublisher;

  @Test
  void start_ChangeStreamSupported_SubmitsChangeStreamTask() {
    when(mongoTemplate.getDb()).thenReturn(mongoDatabase);
    when(mongoDatabase.runCommand(any(Document.class))).thenReturn(new Document("setName", "rs0"));

    doReturn(future).when(executorService).submit(any(Runnable.class));

    outboxPublisher.start();

    verify(pollingPublisher).pollAndSend();
    verify(executorService).submit(any(Runnable.class));
    verify(pollingPublisher, never()).start();
  }

  @Test
  void start_ChangeStreamNotSupported_StartsPollingFallback() {
    when(mongoTemplate.getDb()).thenReturn(mongoDatabase);
    when(mongoDatabase.runCommand(any(Document.class)))
        .thenThrow(new RuntimeException("Not a replica set"));

    outboxPublisher.start();

    verify(pollingPublisher).pollAndSend();
    verify(pollingPublisher).start();
    verify(executorService, never()).submit(any(Runnable.class));
  }

  @Test
  void shutdown_ChangeStreamTaskRunning_CancelsFuture() {
    when(mongoTemplate.getDb()).thenReturn(mongoDatabase);
    when(mongoDatabase.runCommand(any(Document.class))).thenReturn(new Document("setName", "rs0"));

    doReturn(future).when(executorService).submit(any(Runnable.class));

    outboxPublisher.start();

    outboxPublisher.shutdown();

    verify(future).cancel(true);
  }

  @Test
  void shutdown_NoChangeStreamTask_DoesNothing() {
    assertDoesNotThrow(() -> outboxPublisher.shutdown());
    verifyNoInteractions(executorService);
  }
}
