package it.portus.smartorder.ms.invservice.business.stream.conf;

import it.portus.smartorder.events.OrderConfirmedEvent;
import it.portus.smartorder.events.OrderCreatedEvent;
import it.portus.smartorder.events.OrderOutOfStockEvent;
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

  @Bean
  public Consumer<OrderCreatedEvent> orderCreatedConsumer() {
    return event -> {
      log.info("Received order {} in inventory service", event.getOrderId());

      boolean available = checkInventory(event.getOrderId());

      if (available) {
        log.debug("Order {} is available, sending confirmation", event.getOrderId());
        sendConfirmation(event.getOrderId(), true, null);
      } else {
        log.warn("Order {} is not available, sending rejection", event.getOrderId());
        sendConfirmation(event.getOrderId(), false, "Insufficient stock");
      }
    };
  }

  private boolean checkInventory(String orderId) {
    // TODO: implement real inventory check
    return true;
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
