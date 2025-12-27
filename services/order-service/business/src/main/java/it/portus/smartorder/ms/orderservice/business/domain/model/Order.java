package it.portus.smartorder.ms.orderservice.business.domain.model;

import com.querydsl.core.annotations.QueryEntity;
import it.portus.business.commons.model.Entity;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(
    collection = "#{@environment.getProperty('spring.data.mongodb.collection-name', 'order')}")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@QueryEntity
public class Order extends AbstractAggregateRoot<Order> implements Entity<ObjectId> {

  @Id private ObjectId id;

  private String description;

  @NotNull @Builder.Default private OrderState state = OrderState.builder().build();

  @NotNull
  @CreatedDate
  @Setter(AccessLevel.NONE)
  private LocalDateTime createdDate;

  @NotNull
  @LastModifiedDate
  @Setter(AccessLevel.NONE)
  private LocalDateTime lastModifiedDate;
}
