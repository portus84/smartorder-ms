package it.portus.smartorder.ms.orderservice.business.stream.publisher;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import jakarta.annotation.PreDestroy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

  private final MongoTemplate mongoTemplate;
  private final OutboxPollingPublisher pollingPublisher;
  private final OutboxSender outboxSender;

  private Thread changeStreamThread;

  @EventListener(ApplicationReadyEvent.class)
  public void start() {
    log.info("OutboxPublisher STARTING after context ready");

    pollingPublisher.pollAndSend();

    if (isChangeStreamSupported()) {
      log.debug("Mongo Change Stream supported → starting listener");
      startChangeStreamListener();
    } else {
      log.warn("Mongo Change Stream NOT supported → falling back to polling");
      pollingPublisher.start();
    }
  }

  @PreDestroy
  public void shutdown() {
    if (changeStreamThread != null) {
      changeStreamThread.interrupt();
    }
  }

  private void startChangeStreamListener() {
    changeStreamThread = new Thread(this::listenToChangeStream, "OutboxChangeStreamThread");

    changeStreamThread.setDaemon(true);
    changeStreamThread.start();
  }

  private void listenToChangeStream() {
    MongoCollection<Document> collection =
        mongoTemplate.getCollection(
            mongoTemplate.getCollectionName(
                it.portus.smartorder.ms.orderservice.business.domain.events.OrderOutboxEvent
                    .class));

    try (MongoCursor<ChangeStreamDocument<Document>> cursor =
        collection
            .watch(List.of(Aggregates.match(Filters.eq("operationType", "insert"))))
            .fullDocument(FullDocument.DEFAULT)
            .iterator()) {
      cursor.forEachRemaining(
          change -> {
            Document fullDoc = change.getFullDocument();
            if (fullDoc == null) {
              return;
            }

            var outboxEvent =
                mongoTemplate
                    .getConverter()
                    .read(
                        it.portus.smartorder.ms.orderservice.business.domain.events.OrderOutboxEvent
                            .class,
                        fullDoc);

            if (outboxEvent.getStatus()
                != it.portus.smartorder.ms.orderservice.business.domain.events.EventStatus
                    .PENDING) {
              return;
            }

            log.trace("Event id from DB/ChangeStream: {}", outboxEvent.getId());
            outboxSender.sendAsync(outboxEvent);
          });

    } catch (Exception e) {
      if (!Thread.currentThread().isInterrupted()) {
        log.error("Change Stream listener failed", e);
      }
    }
  }

  private boolean isChangeStreamSupported() {
    try {
      Document isMaster = mongoTemplate.getDb().runCommand(new Document("isMaster", 1));

      return isMaster.containsKey("setName");
    } catch (Exception e) {
      return false;
    }
  }
}
