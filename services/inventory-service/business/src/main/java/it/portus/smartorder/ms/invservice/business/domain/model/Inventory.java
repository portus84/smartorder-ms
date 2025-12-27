package it.portus.smartorder.ms.invservice.business.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@EntityListeners(AuditingEntityListener.class)
public class Inventory implements it.portus.business.commons.model.Entity<UUID> {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String description;

  @NotNull
  @Builder.Default
  @Enumerated(EnumType.STRING)
  private InventoryStatus status = InventoryStatus.PENDING;

  @CreatedDate
  @Setter(AccessLevel.NONE)
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdDate;

  @LastModifiedDate
  @Setter(AccessLevel.NONE)
  @Column(nullable = false)
  private LocalDateTime lastModifiedDate;
}
