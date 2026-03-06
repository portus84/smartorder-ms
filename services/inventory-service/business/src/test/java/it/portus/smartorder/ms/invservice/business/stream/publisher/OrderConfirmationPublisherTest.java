package it.portus.smartorder.ms.invservice.business.stream.publisher;

import static org.mockito.Mockito.*;

import it.portus.smartorder.events.OrderConfirmedEvent;
import it.portus.smartorder.events.OrderCreatedEvent;
import it.portus.smartorder.events.OrderOutOfStockEvent;
import it.portus.smartorder.ms.invservice.business.services.InventoryService;
import it.portus.smartorder.ms.invservice.business.stream.BindingNames;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

@ExtendWith(MockitoExtension.class)
class OrderConfirmationPublisherTest {

  @Mock private StreamBridge streamBridge;
  @Mock private InventoryService inventoryService;

  private final OrderConfirmationPublisher orderConfirmationPublisher =
      new OrderConfirmationPublisher(streamBridge, inventoryService);

  @Test
  void send_OrderAvailable_SendsOrderConfirmedEvent() {
    String orderId = "123";
    OrderCreatedEvent event = new OrderCreatedEvent();
    event.setOrderId(orderId);

    when(inventoryService.checkAvailability(orderId)).thenReturn(true);

    orderConfirmationPublisher.send(event);

    ArgumentCaptor<OrderConfirmedEvent> captor = ArgumentCaptor.forClass(OrderConfirmedEvent.class);
    verify(streamBridge).send(eq(BindingNames.PUBLISH_ORDER_CONFIRMED), captor.capture());

    OrderConfirmedEvent sentEvent = captor.getValue();
    assert sentEvent.getOrderId().equals(orderId);

    verify(streamBridge, never()).send(eq(BindingNames.PUBLISH_ORDER_OUT_OF_STOCK), any());
  }

  @Test
  void send_OrderNotAvailable_SendsOrderOutOfStockEvent() {
    String orderId = "456";
    OrderCreatedEvent event = new OrderCreatedEvent();
    event.setOrderId(orderId);

    when(inventoryService.checkAvailability(orderId)).thenReturn(false);

    orderConfirmationPublisher.send(event);

    ArgumentCaptor<OrderOutOfStockEvent> captor =
        ArgumentCaptor.forClass(OrderOutOfStockEvent.class);
    verify(streamBridge).send(eq(BindingNames.PUBLISH_ORDER_OUT_OF_STOCK), captor.capture());

    OrderOutOfStockEvent sentEvent = captor.getValue();
    assert sentEvent.getOrderId().equals(orderId);
    assert sentEvent.getReason().equals("Insufficient stock");

    verify(streamBridge, never()).send(eq(BindingNames.PUBLISH_ORDER_CONFIRMED), any());
  }
}
