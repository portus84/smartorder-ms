package it.portus.smartorder.ms.orderservice.business.stream.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.portus.smartorder.events.OrderConfirmedEvent;
import it.portus.smartorder.events.OrderOutOfStockEvent;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import it.portus.smartorder.ms.orderservice.business.services.OrderService;
import it.portus.smartorder.ms.orderservice.business.stream.adapter.impl.OrderEventAdapterImpl;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderEventAdapterTest {

  @Mock private OrderService orderService;

  private OrderEventAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new OrderEventAdapterImpl(orderService);
  }

  @Test
  void onOrderConfirmed_OrderExists_UpdatesStateToConfirmed() {
    Order order = new Order();
    String orderId = order.getId().toString();

    OrderConfirmedEvent event = new OrderConfirmedEvent();
    event.setOrderId(orderId);

    when(orderService.findById(order.getId())).thenReturn(Optional.of(order));

    adapter.onOrderConfirmed(event);

    assertNotNull(order.getState());
    assertEquals(OrderStatus.CONFIRMED, order.getState().getStatus());
    assertNull(order.getState().getReason());

    verify(orderService).update(order.getId(), order);
  }

  @Test
  void onOrderConfirmed_OrderDoesNotExist_DoesNotCallUpdate() {
    String orderId = UUID.randomUUID().toString();
    OrderConfirmedEvent event = new OrderConfirmedEvent();
    event.setOrderId(orderId);

    when(orderService.findById(UUID.fromString(orderId))).thenReturn(Optional.empty());

    adapter.onOrderConfirmed(event);

    verify(orderService, never()).update(any(), any());
  }

  @Test
  void onOrderOutOfStock_OrderExists_UpdatesStateToOutOfStock() {
    Order order = new Order();
    String orderId = order.getId().toString();
    String reason = "Insufficient stock";

    OrderOutOfStockEvent event = new OrderOutOfStockEvent();
    event.setOrderId(orderId);
    event.setReason(reason);

    when(orderService.findById(order.getId())).thenReturn(Optional.of(order));

    adapter.onOrderOutOfStock(event);

    assertNotNull(order.getState());
    assertEquals(OrderStatus.OUT_OF_STOCK, order.getState().getStatus());
    assertEquals(reason, order.getState().getReason());

    verify(orderService).update(order.getId(), order);
  }

  @Test
  void onOrderOutOfStock_OrderDoesNotExist_DoesNotCallUpdate() {
    String orderId = UUID.randomUUID().toString();
    OrderOutOfStockEvent event = new OrderOutOfStockEvent();
    event.setOrderId(orderId);
    event.setReason("Out of stock");

    when(orderService.findById(UUID.fromString(orderId))).thenReturn(Optional.empty());

    adapter.onOrderOutOfStock(event);

    verify(orderService, never()).update(any(), any());
  }
}
