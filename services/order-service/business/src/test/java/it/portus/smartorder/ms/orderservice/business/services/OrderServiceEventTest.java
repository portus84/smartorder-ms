/*
package it.portus.smartorder.ms.orderservice.business.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.portus.smartorder.events.OrderCreatedEvent;
import it.portus.smartorder.ms.orderservice.business.domain.events.EventStatus;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.stream.BindingNames;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@Import({TestChannelBinderConfiguration.class})
class OrderServiceEventTest {

  @Autowired private OrderService orderService;
  @Autowired private OutputDestination outputDestination;

  @MockitoSpyBean private StreamBridge streamBridge;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    Mockito.reset(streamBridge);
    outputDestination.clear();
  }

  @Test
  void save_Order_PublishesOrderCreatedEvent_SetsEventStatusSent() throws Exception {
    Order order = Instancio.create(Order.class);

    orderService.save(order);

    Message<byte[]> message = outputDestination.receive(0, BindingNames.PUBLISH_ORDER_CREATED);
    assertNotNull(message, "Message should have been sent");

    OrderCreatedEvent event = objectMapper.readValue(message.getPayload(), OrderCreatedEvent.class);
    assertEquals(order.getId().toHexString(), event.getOrderId());

    Order savedOrder = orderService.findById(order.getId()).orElseThrow();

    assertEquals(EventStatus.SENT, savedOrder.getEventStatus());
  }

  @Test
  void save_Order_WhenSendFails_UpdatesStatusToFailed() {
    Order order = Instancio.create(Order.class);
    order.setEventStatus(EventStatus.PENDING);

    doThrow(new RuntimeException("Simulated failure")).when(streamBridge).send(anyString(), any());

    orderService.save(order);

    Order savedOrder = orderService.findById(order.getId()).orElseThrow();

    assertEquals(EventStatus.FAILED, savedOrder.getEventStatus());
  }
}
*/
