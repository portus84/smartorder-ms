package it.portus.smartorder.ms.orderservice.api.controllers.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = OrdersApiController.class)
@Import({ControllerTestConfig.class, OrdersApiDelegateImpl.class})
@MockitoBean(types = {CacheManager.class})
class GetOrderByIdControllerTest extends AbstractControllerTest {

  private static final String ENDPOINT = "/api/v1/orders/{id}";

  @MockitoBean private OrderService orderService;

  @Override
  protected String endpoint() {
    return ENDPOINT;
  }

  @Test
  @SneakyThrows
  void getOrderById_WhenOrderExists_ReturnsOrder() {
    Order mocked = Instancio.create(Order.class);
    when(orderService.findById(mocked.getId())).thenReturn(Optional.of(mocked));

    String responseJson =
        get(mocked.getId())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    EntityModel<it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order> response =
        objectMapper.readValue(responseJson, new TypeReference<>() {});

    Assertions.assertNotNull(response.getContent());
    assertEquals(mocked.getId(), response.getContent().getId());

    verify(orderService).findById(mocked.getId());
  }

  @Test
  @SneakyThrows
  void getOrderById_WhenOrderNotFound_ReturnsNotFound() {
    UUID id = UUID.randomUUID();
    when(orderService.findById(id)).thenReturn(Optional.empty());

    get(id).andExpect(errorResponse(HttpStatus.NOT_FOUND));
  }

  @Test
  @SneakyThrows
  void getOrderById_WhenInvalidParameterPassed_ReturnsBadRequest() {
    get("bad-id").andExpect(errorResponse(HttpStatus.BAD_REQUEST));
  }

  @Test
  @SneakyThrows
  void getOrderById_WhenInvalidParameterPassed_ReturnsUnprocessableEntity() {
    String message = "Invalid parameter";
    when(orderService.findById(any(UUID.class))).thenThrow(new IllegalArgumentException(message));

    get(UUID.randomUUID()).andExpect(errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, message));
  }

  @Test
  @SneakyThrows
  void getOrderById_WhenDatabaseUnavailable_ReturnsInternalServerErrorWithDetails() {
    String message = "DB unavailable";
    when(orderService.findById(any(UUID.class))).thenThrow(new RuntimeException(message));

    get(UUID.randomUUID()).andExpect(errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message));
  }

  @Test
  @SneakyThrows
  @Disabled("Authorization not implemented yet")
  void getOrderById_WhenUnauthorized_ReturnsUnauthorized() {
    get("anyId").andExpect(status().isUnauthorized());
  }
}
