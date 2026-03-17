package it.portus.smartorder.ms.orderservice.business.domain.repositories;

import it.portus.smartorder.ms.orderservice.business.domain.events.EventStatus;
import it.portus.smartorder.ms.orderservice.business.domain.events.OrderOutboxEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderOutboxRepository extends MongoRepository<OrderOutboxEvent, UUID> {

  List<OrderOutboxEvent> findByStatus(EventStatus status);
}
