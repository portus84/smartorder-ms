package it.portus.smartorder.ms.orderservice.business.rules.order.strategy.impl;

import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.rules.order.strategy.OrderRuleStrategy;
import org.kie.api.runtime.KieSession;

public class SimpleProcessingStrategy implements OrderRuleStrategy {
  @Override
  public Order execute(KieSession session, Order order) {
    session.insert(order);
    session.fireAllRules();
    return order;
  }
}
