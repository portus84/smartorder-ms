package it.portus.smartorder.ms.invservice.business.stream.conf;

import it.portus.smartorder.events.OrderCreatedEvent;
import it.portus.smartorder.ms.invservice.business.stream.publisher.OrderConfirmationPublisher;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderConsumersConfiguration {

  private final OrderConfirmationPublisher orderConfirmationPublisher;

  @Bean
  public Consumer<OrderCreatedEvent> orderCreatedConsumer() {
    return event -> {
      String orderId = event.getOrderId();
      log.info("Received order {} in inventory service", orderId);

      orderConfirmationPublisher.send(event);
    };
  }
}
