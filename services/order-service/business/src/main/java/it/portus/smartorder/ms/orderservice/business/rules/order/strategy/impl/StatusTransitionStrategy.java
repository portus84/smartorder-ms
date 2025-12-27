package it.portus.smartorder.ms.orderservice.business.rules.order.strategy.impl;

import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import it.portus.smartorder.ms.orderservice.business.rules.order.strategy.OrderRuleStrategy;
import it.portus.smartorder.ms.orderservice.business.rules.order.strategy.model.OrderTransition;
import org.kie.api.runtime.KieSession;

public record StatusTransitionStrategy(OrderStatus targetStatus) implements OrderRuleStrategy {

  @Override
  public Order execute(KieSession session, Order order) {
    session.insert(order);
    session.insert(new OrderTransition(order.getId(), targetStatus));

    session.getAgenda().getAgendaGroup("validation").setFocus();
    session.fireAllRules();

    return order;
  }
}
