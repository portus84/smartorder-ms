package it.portus.smartorder.ms.orderservice.business.rules.order.strategy.impl;

import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import it.portus.smartorder.ms.orderservice.business.rules.order.strategy.model.ValidTransition;
import java.util.ArrayList;
import java.util.List;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;

public class TransitionStrategy {

  public List<OrderStatus> execute(KieSession session, Order order) {
    session.insert(order);

    session.getAgenda().getAgendaGroup("validation").setFocus();
    session.fireAllRules();

    QueryResults results =
        session.getQueryResults("validTransitionsFor", order.getState().getStatus());

    List<OrderStatus> transitions = new ArrayList<>();

    for (QueryResultsRow row : results) {
      ValidTransition vt = (ValidTransition) row.get("$vt");
      transitions.add(vt.next());
    }

    return transitions;
  }
}
