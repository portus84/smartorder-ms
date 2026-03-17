package it.portus.smartorder.ms.invservice.business.stream.publisher;

import it.portus.business.commons.stream.publisher.EventPublisher;
import it.portus.smartorder.events.OrderConfirmedEvent;
import it.portus.smartorder.events.OrderCreatedEvent;
import it.portus.smartorder.events.OrderOutOfStockEvent;
import it.portus.smartorder.ms.invservice.business.services.InventoryService;
import it.portus.smartorder.ms.invservice.business.stream.BindingNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConfirmationPublisher implements EventPublisher<OrderCreatedEvent> {

  private final InventoryService inventoryService;

  private final StreamBridge streamBridge;

  @Override
  public void publish(OrderCreatedEvent event) {
    boolean available = checkInventory(event);

    String orderId = event.getOrderId();

    if (available) {
      log.debug("Order {} is available, publishing confirmation", orderId);
      sendConfirmation(orderId, true, null);
    } else {
      log.warn("Order {} is not available, publishing rejection", orderId);
      sendConfirmation(orderId, false, "Insufficient stock");
    }
  }

  private boolean checkInventory(OrderCreatedEvent event) {
    return inventoryService.checkAvailability(event.getOrderId());
  }

  private void sendConfirmation(String orderId, boolean confirmed, String reason) {
    if (confirmed) {
      OrderConfirmedEvent event = new OrderConfirmedEvent();
      event.setOrderId(orderId);

      streamBridge.send(BindingNames.PUBLISH_ORDER_CONFIRMED, event);
    } else {
      OrderOutOfStockEvent event = new OrderOutOfStockEvent();
      event.setOrderId(orderId);
      event.setReason(reason);

      streamBridge.send(BindingNames.PUBLISH_ORDER_OUT_OF_STOCK, event);
    }
  }
}
