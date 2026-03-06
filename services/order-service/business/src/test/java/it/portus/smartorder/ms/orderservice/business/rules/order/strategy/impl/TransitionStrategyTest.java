package it.portus.smartorder.ms.orderservice.business.rules.order.strategy.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderState;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import it.portus.smartorder.ms.orderservice.business.rules.order.strategy.model.ValidTransition;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.Agenda;
import org.kie.api.runtime.rule.AgendaGroup;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransitionStrategyTest {

  @Mock private KieSession kieSession;
  @Mock private Agenda agenda;
  @Mock private AgendaGroup validationGroup;

  private TransitionStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new TransitionStrategy();
    when(kieSession.getAgenda()).thenReturn(agenda);
    when(agenda.getAgendaGroup("validation")).thenReturn(validationGroup);
    doNothing().when(validationGroup).setFocus();
  }

  @Test
  void execute_OrderInPendingState_ReturnsAllValidNextStatuses() {
    Order order =
        Order.builder().state(OrderState.builder().status(OrderStatus.PENDING).build()).build();

    List<OrderStatus> validStatuses =
        Arrays.asList(
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.CANCELLED,
            OrderStatus.OUT_OF_STOCK);

    List<QueryResultsRow> rows =
        validStatuses.stream()
            .map(
                status -> {
                  ValidTransition vt = mock(ValidTransition.class);
                  when(vt.next()).thenReturn(status);

                  QueryResultsRow row = mock(QueryResultsRow.class);
                  when(row.get("$vt")).thenReturn(vt);
                  return row;
                })
            .toList();

    QueryResults queryResults = mock(QueryResults.class);
    when(queryResults.iterator()).thenReturn(rows.iterator());
    when(kieSession.getQueryResults("validTransitionsFor", OrderStatus.PENDING))
        .thenReturn(queryResults);

    List<OrderStatus> result = strategy.execute(kieSession, order);

    assertEquals(validStatuses, result);

    verify(kieSession).insert(order);
    verify(validationGroup).setFocus();
    verify(kieSession).fireAllRules();
    verify(kieSession).getQueryResults("validTransitionsFor", OrderStatus.PENDING);
  }
}
