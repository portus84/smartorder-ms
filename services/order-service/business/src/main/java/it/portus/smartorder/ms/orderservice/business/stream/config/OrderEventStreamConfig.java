package it.portus.smartorder.ms.orderservice.business.stream.config;

import it.portus.smartorder.events.OrderConfirmedEvent;
import it.portus.smartorder.events.OrderOutOfStockEvent;
import it.portus.smartorder.ms.orderservice.business.stream.adapter.OrderEventAdapter;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderEventStreamConfig {

  private final OrderEventAdapter eventAdapter;

  @Bean
  public Consumer<OrderConfirmedEvent> orderConfirmedConsumer() {
    return eventAdapter::onOrderConfirmed;
  }

  @Bean
  public Consumer<OrderOutOfStockEvent> orderOutOfStockConsumer() {
    return eventAdapter::onOrderOutOfStock;
  }
}
