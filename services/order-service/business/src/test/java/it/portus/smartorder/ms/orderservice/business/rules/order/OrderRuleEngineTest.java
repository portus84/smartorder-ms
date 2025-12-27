package it.portus.smartorder.ms.orderservice.business.rules.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import it.portus.smartorder.ms.orderservice.business.rules.order.impl.OrderRuleEngineImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.Agenda;
import org.kie.api.runtime.rule.AgendaGroup;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class})
class OrderRuleEngineTest {

  @Mock private KieSession kieSession;
  @Mock private KieBase kieBase;

  @Mock private Agenda agenda;
  @Mock private AgendaGroup validationGroup;

  private OrderRuleEngine ruleEngine;

  @BeforeEach
  void setUp() {
    when(kieBase.newKieSession()).thenReturn(kieSession);
    when(kieSession.fireAllRules()).thenReturn(0);

    ruleEngine = new OrderRuleEngineImpl(kieBase);
  }

  @Test
  void applyRules_withoutStatus_returnsSameOrder() {
    Order order = Order.builder().description("Test Order").build();

    Order result = ruleEngine.applyRules(order);

    assertEquals(order, result, "The order should be returned unchanged");
    verify(kieSession).insert(order);
    verify(kieSession).fireAllRules();
  }

  @Test
  void applyStatusTransition_withStatus_returnsSameOrder() {
    when(kieSession.getAgenda()).thenReturn(agenda);
    when(agenda.getAgendaGroup("validation")).thenReturn(validationGroup);

    doNothing().when(validationGroup).setFocus();

    Order order = Order.builder().description("Test Order").build();

    OrderStatus newStatus = OrderStatus.CONFIRMED;
    Order result = ruleEngine.applyStatusTransition(order, newStatus);

    assertEquals(order, result, "The order should be returned unchanged");
    verify(kieSession, atLeastOnce()).insert(order);
    verify(kieSession, atLeast(1)).fireAllRules();
  }
}
