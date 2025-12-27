package it.portus.smartorder.ms.orderservice.business.rules.order;

import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import java.util.List;

public interface OrderRuleEngine {

  Order applyRules(Order order);

  Order applyStatusTransition(Order order, OrderStatus newStatus);

  List<OrderStatus> getTransitionStatuses(Order order);
}
