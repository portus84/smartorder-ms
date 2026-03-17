package it.portus.smartorder.ms.orderservice.business.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("order-outbox-event")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderOutboxEvent {

  @Id @Builder.Default private UUID id = UUID.randomUUID();

  private UUID aggregateId;
  private String eventType;
  private String payload;
  private EventStatus status;

  @CreatedDate private LocalDateTime createdDate;

  @LastModifiedDate private LocalDateTime lastModifiedDate;
}
