package it.portus.smartorder.ms.invservice.business.stream.conf;

import it.portus.smartorder.events.OrderConfirmedEvent;
import it.portus.smartorder.events.OrderCreatedEvent;
import it.portus.smartorder.events.OrderOutOfStockEvent;
import it.portus.smartorder.ms.invservice.business.services.InventoryService;
import it.portus.smartorder.ms.invservice.business.stream.BindingNames;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderConsumersConfiguration {

  private final StreamBridge streamBridge;

  private final InventoryService inventoryService;

  @Bean
  public Consumer<OrderCreatedEvent> orderCreatedConsumer() {
    return event -> {
      String orderId = event.getOrderId();

      log.info("Received order {} in inventory service", orderId);

      boolean available = checkInventory(event);

      if (available) {
        log.debug("Order {} is available, sending confirmation", orderId);
        sendConfirmation(orderId, true, null);
      } else {
        log.warn("Order {} is not available, sending rejection", orderId);
        sendConfirmation(orderId, false, "Insufficient stock");
      }
    };
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
