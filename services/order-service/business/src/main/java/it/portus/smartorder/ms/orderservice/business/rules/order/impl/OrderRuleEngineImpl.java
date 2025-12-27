package it.portus.smartorder.ms.orderservice.business.rules.order.impl;

import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import it.portus.smartorder.ms.orderservice.business.rules.order.OrderRuleEngine;
import it.portus.smartorder.ms.orderservice.business.rules.order.strategy.OrderRuleStrategy;
import it.portus.smartorder.ms.orderservice.business.rules.order.strategy.impl.SimpleProcessingStrategy;
import it.portus.smartorder.ms.orderservice.business.rules.order.strategy.impl.StatusTransitionStrategy;
import it.portus.smartorder.ms.orderservice.business.rules.order.strategy.impl.TransitionStrategy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderRuleEngineImpl implements OrderRuleEngine {

  private final KieBase orderKieBase;

  @Override
  public Order applyRules(Order order) {
    return executeWithStrategy(order, new SimpleProcessingStrategy());
  }

  @Override
  public Order applyStatusTransition(Order order, OrderStatus newStatus) {
    return executeWithStrategy(order, new StatusTransitionStrategy(newStatus));
  }

  @Override
  public List<OrderStatus> getTransitionStatuses(Order order) {
    try (KieSession session = orderKieBase.newKieSession()) {
      return new TransitionStrategy().execute(session, order);
    }
  }

  private Order executeWithStrategy(Order order, OrderRuleStrategy strategy) {
    try (KieSession session = orderKieBase.newKieSession()) {
      return strategy.execute(session, order);
    }
  }
}
