package it.portus.smartorder.ms.orderservice.business.rules.order.strategy.model;

import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;

public record ValidTransition(OrderStatus current, OrderStatus next) {}
