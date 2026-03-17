package it.portus.smartorder.ms.orderservice.business.stream.listeners;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import it.portus.business.commons.stream.publisher.EventPublisher;
import it.portus.smartorder.ms.orderservice.business.domain.events.OrderOutboxEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangeMongoStreamListener {

  private final MongoTemplate mongoTemplate;
  private final EventPublisher<OrderOutboxEvent> publisher;

  public boolean isChangeStreamSupported() {
    try {
      Document isMaster = mongoTemplate.getDb().runCommand(new Document("isMaster", 1));

      return isMaster.containsKey("setName");
    } catch (Exception e) {
      return false;
    }
  }

  public void listen() {
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
            if (Thread.currentThread().isInterrupted()) {
              log.error("Change Stream listener is interrupted");
              return;
            }

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
            publisher.publish(outboxEvent);
          });

    } catch (Exception e) {
      if (!Thread.currentThread().isInterrupted()) {
        log.error("Change Stream listener failed", e);
      }
    }
  }
}
