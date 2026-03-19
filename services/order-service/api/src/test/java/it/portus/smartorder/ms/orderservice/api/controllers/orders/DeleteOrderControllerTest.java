package it.portus.smartorder.ms.orderservice.api.controllers.orders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import it.portus.ms.test.controller.AbstractControllerTest;
import it.portus.smartorder.ms.orderservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.orderservice.api.controller.impl.OrdersApiDelegateImpl;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.OrdersApiController;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.services.OrderService;
import java.util.Optional;
import java.util.UUID;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = OrdersApiController.class)
@Import({ControllerTestConfig.class, OrdersApiDelegateImpl.class})
@MockitoBean(types = {CacheManager.class})
class DeleteOrderControllerTest extends AbstractControllerTest {

  private static final String ENDPOINT = "/api/v1/orders/{id}";

  @MockitoBean private OrderService orderService;

  @Test
  @SneakyThrows
  void deleteOrder_WhenOrderExists_ReturnsNoContent() {
    UUID id = UUID.randomUUID();
    when(orderService.findById(id)).thenReturn(Optional.of(Instancio.create(Order.class)));
    doNothing().when(orderService).deleteById(id);

    delete(ENDPOINT, id).andExpect(status().isNoContent());
  }

  @Test
  @SneakyThrows
  void deleteOrder_WhenOrderNotFound_ReturnsNotFound() {
    UUID id = UUID.randomUUID();
    when(orderService.findById(id)).thenReturn(Optional.empty());

    delete(ENDPOINT, id).andExpect(errorResponse(HttpStatus.NOT_FOUND));
  }

  @Test
  @SneakyThrows
  void deleteOrder_WhenInvalidIdPassed_ReturnsBadRequest() {
    delete(ENDPOINT, "bad-id").andExpect(errorResponse(HttpStatus.BAD_REQUEST));
  }

  @Test
  @SneakyThrows
  void deleteOrder_WhenServiceThrowsIllegalArgument_ReturnsUnprocessableEntity() {
    String message = "Invalid parameter";
    when(orderService.findById(any(UUID.class))).thenThrow(new IllegalArgumentException(message));

    delete(ENDPOINT, UUID.randomUUID())
        .andExpect(errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, message));
  }

  @Test
  @SneakyThrows
  void deleteOrder_WhenDatabaseUnavailable_ReturnsInternalServerErrorWithDetails() {
    String message = "DB unavailable";
    when(orderService.findById(any(UUID.class))).thenThrow(new RuntimeException(message));

    delete(ENDPOINT, UUID.randomUUID())
        .andExpect(errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message));
  }
}
