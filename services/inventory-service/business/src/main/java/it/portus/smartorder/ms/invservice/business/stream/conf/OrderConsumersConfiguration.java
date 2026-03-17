package it.portus.smartorder.ms.invservice.business.stream.conf;

import it.portus.business.commons.stream.publisher.EventPublisher;
import it.portus.smartorder.events.OrderCreatedEvent;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderConsumersConfiguration {

  private final EventPublisher<OrderCreatedEvent> orderConfirmationPublisher;

  @Bean
  public Consumer<OrderCreatedEvent> orderCreatedConsumer() {
    return event -> {
      String orderId = event.getOrderId();
      log.info("Received order {} in inventory service", orderId);

      orderConfirmationPublisher.publish(event);
    };
  }
}
