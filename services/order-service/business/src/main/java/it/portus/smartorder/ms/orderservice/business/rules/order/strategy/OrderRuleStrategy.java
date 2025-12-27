package it.portus.smartorder.ms.orderservice.business.rules.order.strategy;

import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import org.kie.api.runtime.KieSession;

public interface OrderRuleStrategy {
  Order execute(KieSession session, Order order);
}
