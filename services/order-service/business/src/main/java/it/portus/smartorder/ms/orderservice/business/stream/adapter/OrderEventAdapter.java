package it.portus.smartorder.ms.orderservice.business.stream.adapter;

import it.portus.smartorder.events.OrderConfirmedEvent;
import it.portus.smartorder.events.OrderOutOfStockEvent;

public interface OrderEventAdapter {

  void onOrderConfirmed(OrderConfirmedEvent event);

  void onOrderOutOfStock(OrderOutOfStockEvent event);
}
