package it.portus.smartorder.ms.orderservice.api.controllers.orders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import it.portus.ms.test.controller.AbstractControllerTest;
import it.portus.smartorder.ms.orderservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.orderservice.api.controller.impl.OrdersApiDelegateImpl;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.OrdersApiController;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.model.UpdateOrderRequest;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.services.OrderService;
import java.util.Optional;
import java.util.UUID;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = OrdersApiController.class)
@Import({ControllerTestConfig.class, OrdersApiDelegateImpl.class})
@MockitoBean(types = {CacheManager.class})
class UpdateOrderControllerTest extends AbstractControllerTest {

  private static final String ENDPOINT = "/api/v1/orders/{id}";

  @MockitoBean private OrderService orderService;

  @Override
  protected String endpoint() {
    return ENDPOINT;
  }

  @Test
  @SneakyThrows
  void updateOrder_WhenValidOrderProvided_ReturnsUpdatedOrder() {
    Order mockedOrder = Instancio.create(Order.class);

    when(orderService.update(any(UUID.class), any(Order.class)))
        .thenReturn(Optional.of(mockedOrder));

    String responseJson =
        put(Instancio.create(UpdateOrderRequest.class), mockedOrder.getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.createdDate").exists())
            .andExpect(jsonPath("$.lastModifiedDate").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

    EntityModel<it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order> response =
        objectMapper.readValue(responseJson, new TypeReference<>() {});

    it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order content = response.getContent();
    Assertions.assertNotNull(content);
    assertEquals(mockedOrder.getId(), content.getId());
    assertTrue(response.getLink(IanaLinkRelations.SELF.value()).isPresent());

    verify(orderService).update(any(UUID.class), any(Order.class));
  }

  @Test
  @SneakyThrows
  void updateOrder_WhenOrderDoesNotExist_ReturnsNotFound() {
    when(orderService.update(any(UUID.class), any(Order.class))).thenReturn(Optional.empty());

    put(Instancio.create(UpdateOrderRequest.class), UUID.randomUUID())
        .andExpect(errorResponse(HttpStatus.NOT_FOUND));
  }

  @ParameterizedTest(name = "{0} => BadRequest")
  @ValueSource(strings = {"{ invalid json }", "{ }"})
  @SneakyThrows
  void updateOrder_WhenInvalidRequestBody_ReturnsBadRequest(String body) {
    put(body, UUID.randomUUID()).andExpect(errorResponse(HttpStatus.BAD_REQUEST));
  }

  @Test
  @SneakyThrows
  void updateOrder_WhenValidationFails_ReturnsUnprocessableEntity() {
    String message = "Invalid data";
    when(orderService.update(any(UUID.class), any(Order.class)))
        .thenThrow(new IllegalArgumentException(message));

    put(Instancio.create(UpdateOrderRequest.class), UUID.randomUUID())
        .andExpect(errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, message));
  }

  @Test
  @SneakyThrows
  void updateOrder_WhenDatabaseUnavailable_ReturnsInternalServerError() {
    String message = "DB unavailable";
    when(orderService.update(any(UUID.class), any(Order.class)))
        .thenThrow(new RuntimeException(message));

    put(Instancio.create(UpdateOrderRequest.class), UUID.randomUUID())
        .andExpect(errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message));
  }
}
