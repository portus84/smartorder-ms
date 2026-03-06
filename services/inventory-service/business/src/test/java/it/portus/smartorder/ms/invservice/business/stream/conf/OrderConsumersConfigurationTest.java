package it.portus.smartorder.ms.invservice.business.stream.conf;

import static org.mockito.Mockito.*;

import it.portus.smartorder.events.OrderConfirmedEvent;
import it.portus.smartorder.events.OrderCreatedEvent;
import it.portus.smartorder.events.OrderOutOfStockEvent;
import it.portus.smartorder.ms.invservice.business.services.InventoryService;
import it.portus.smartorder.ms.invservice.business.stream.BindingNames;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderConsumersConfigurationTest {

  @Mock private org.springframework.cloud.stream.function.StreamBridge streamBridge;
  @Mock private InventoryService inventoryService;

  private Consumer<OrderCreatedEvent> consumer;

  @BeforeEach
  void setUp() {
    OrderConsumersConfiguration config =
        new OrderConsumersConfiguration(streamBridge, inventoryService);
    consumer = config.orderCreatedConsumer();
  }

  @Test
  void orderCreatedConsumer_OrderAvailable_SendsOrderConfirmedEvent() {
    String orderId = "123";
    OrderCreatedEvent event = new OrderCreatedEvent();
    event.setOrderId(orderId);

    when(inventoryService.checkAvailability(orderId)).thenReturn(true);

    consumer.accept(event);

    ArgumentCaptor<OrderConfirmedEvent> captor = ArgumentCaptor.forClass(OrderConfirmedEvent.class);
    verify(streamBridge).send(eq(BindingNames.PUBLISH_ORDER_CONFIRMED), captor.capture());

    OrderConfirmedEvent sentEvent = captor.getValue();
    assert sentEvent.getOrderId().equals(orderId);

    verify(streamBridge, never()).send(eq(BindingNames.PUBLISH_ORDER_OUT_OF_STOCK), any());
  }

  @Test
  void orderCreatedConsumer_OrderNotAvailable_SendsOrderOutOfStockEvent() {
    String orderId = "456";
    OrderCreatedEvent event = new OrderCreatedEvent();
    event.setOrderId(orderId);

    when(inventoryService.checkAvailability(orderId)).thenReturn(false);

    consumer.accept(event);

    ArgumentCaptor<OrderOutOfStockEvent> captor =
        ArgumentCaptor.forClass(OrderOutOfStockEvent.class);
    verify(streamBridge).send(eq(BindingNames.PUBLISH_ORDER_OUT_OF_STOCK), captor.capture());

    OrderOutOfStockEvent sentEvent = captor.getValue();
    assert sentEvent.getOrderId().equals(orderId);
    assert sentEvent.getReason().equals("Insufficient stock");

    verify(streamBridge, never()).send(eq(BindingNames.PUBLISH_ORDER_CONFIRMED), any());
  }
}
