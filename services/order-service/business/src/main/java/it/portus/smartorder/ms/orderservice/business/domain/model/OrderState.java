package it.portus.smartorder.ms.orderservice.business.domain.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OrderState {

  @NotNull @Builder.Default private OrderStatus status = OrderStatus.PENDING;

  private String reason;
}
