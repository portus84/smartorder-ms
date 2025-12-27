package it.portus.smartorder.ms.orderservice.business.rules.order.strategy.model;

import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import org.bson.types.ObjectId;

public record OrderTransition(ObjectId orderId, OrderStatus targetStatus) {}
