package it.portus.smartorder.ms.orderservice.api.controllers.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import it.portus.ms.test.controller.AbstractControllerTest;
import it.portus.smartorder.ms.orderservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.orderservice.api.controller.impl.OrdersApiDelegateImpl;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.OrdersApiController;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.model.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = OrdersApiController.class)
@Import({ControllerTestConfig.class, OrdersApiDelegateImpl.class})
@MockitoBean(types = {CacheManager.class})
class PatchOrderControllerTest extends AbstractControllerTest {

  private static final String ENDPOINT = "/api/v1/orders/{id}";

  @MockitoBean private OrderService orderService;

  @Test
  @SneakyThrows
  void patchOrder_WhenValidDetailsPatchProvided_ReturnsPatchedOrder() {
    Order existing = Instancio.create(Order.class);
    Order updated = Instancio.create(Order.class);

    when(orderService.findById(any(UUID.class))).thenReturn(Optional.of(existing));
    when(orderService.update(any(UUID.class), any(Order.class))).thenReturn(Optional.of(updated));

    OrderDetailsPatchOperation patchOp =
        new OrderDetailsPatchOperation()
            .operation(OrderPatchRequestOperation.UPDATE_DETAILS)
            .details(Instancio.create(OrderDetailsPatchOperationDetails.class));

    verifyPatchResponse(patchOp, existing, updated);
  }

  @Test
  @SneakyThrows
  void patchOrder_WhenValidStatusPatchProvided_ReturnsPatchedOrder() {
    Order existing = Instancio.create(Order.class);
    Order updated = Instancio.create(Order.class);

    when(orderService.findById(any(UUID.class))).thenReturn(Optional.of(existing));
    when(orderService.update(any(UUID.class), any(Order.class))).thenReturn(Optional.of(updated));

    OrderStatusPatchOperation patchOp =
        new OrderStatusPatchOperation()
            .operation(OrderPatchRequestOperation.UPDATE_STATUS)
            .status(Instancio.create(OrderStatus.class));

    verifyPatchResponse(patchOp, existing, updated);
  }

  @Test
  @SneakyThrows
  void patchOrder_WhenOrderDoesNotExist_ReturnsNotFound() {
    when(orderService.findById(any(UUID.class))).thenReturn(Optional.empty());

    patch(ENDPOINT, Instancio.create(OrderDetailsPatchOperation.class), UUID.randomUUID())
        .andExpect(errorResponse(HttpStatus.NOT_FOUND));
  }

  @ParameterizedTest(name = "{0} => BadRequest")
  @ValueSource(
      strings = {
        "{ invalid json }",
        "{ }",
        """
        {"details": { "description": "test" }}
        """,
        """
        { "operation": "DO_NOT_EXIST", "details": { "description": "test" } }
        """,
      })
  @SneakyThrows
  void patchOrder_WhenInvalidRequestBody_ReturnsBadRequest(String body) {
    patch(ENDPOINT, body, UUID.randomUUID()).andExpect(errorResponse(HttpStatus.BAD_REQUEST));
  }

  @Test
  @SneakyThrows
  void patchOrder_WhenValidationFails_ReturnsUnprocessableEntity() {
    String message = "Invalid patch data";
    Order existing = Instancio.create(Order.class);

    when(orderService.findById(any(UUID.class))).thenReturn(Optional.of(existing));
    when(orderService.update(any(UUID.class), any(Order.class)))
        .thenThrow(new IllegalArgumentException(message));

    patch(ENDPOINT, Instancio.create(OrderDetailsPatchOperation.class), existing.getId())
        .andExpect(errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, message));
  }

  @Test
  @SneakyThrows
  void patchOrder_WhenDatabaseUnavailable_ReturnsInternalServerError() {
    String message = "DB unavailable";
    Order existing = Instancio.create(Order.class);

    when(orderService.findById(any(UUID.class))).thenReturn(Optional.of(existing));
    when(orderService.update(any(UUID.class), any(Order.class)))
        .thenThrow(new RuntimeException(message));

    patch(ENDPOINT, Instancio.create(OrderDetailsPatchOperation.class), existing.getId())
        .andExpect(errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message));
  }

  @SneakyThrows
  private <T> void verifyPatchResponse(T patchOp, Order actual, Order expected) {
    String responseJson =
        patch(ENDPOINT, patchOp, actual.getId())
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

    assertEquals(expected.getId(), content.getId());

    verify(orderService).findById(any(UUID.class));
    verify(orderService).update(any(UUID.class), any(Order.class));
  }
}
