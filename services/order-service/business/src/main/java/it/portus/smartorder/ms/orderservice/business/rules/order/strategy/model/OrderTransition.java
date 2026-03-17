package it.portus.smartorder.ms.orderservice.business.rules.order.strategy.model;

import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import java.util.UUID;

public record OrderTransition(UUID orderId, OrderStatus targetStatus) {}
