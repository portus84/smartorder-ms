package it.portus.smartorder.ms.orderservice.api.controllers.orders;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.portus.smartorder.ms.orderservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.orderservice.api.controller.impl.OrdersApiDelegateImpl;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.OrdersApiController;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.model.*;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.services.OrderService;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrdersApiController.class)
@Import({ControllerTestConfig.class, OrdersApiDelegateImpl.class})
@MockitoBean(types = {CacheManager.class})
class PatchOrderControllerTest {

  private static final String ENDPOINT = "/api/v1/orders";

  @Autowired private MockMvc mockMvc;
  @MockitoBean private OrderService orderService;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void patchOrder_WhenValidDetailsPatchProvided_ReturnsPatchedOrder() throws Exception {
    Order existing = Instancio.create(Order.class);
    Order updated = Instancio.create(Order.class);

    when(orderService.findById(any(UUID.class))).thenReturn(Optional.of(existing));
    when(orderService.update(any(UUID.class), any(Order.class))).thenReturn(Optional.of(updated));

    OrderDetailsPatchOperation patchOp =
        new OrderDetailsPatchOperation()
            .operation(OrderPatchRequestOperation.UPDATE_DETAILS)
            .details(Instancio.create(OrderDetailsPatchOperationDetails.class));

    String requestJson = objectMapper.writeValueAsString(patchOp);

    String responseJson =
        mockMvc
            .perform(
                patch(ENDPOINT + "/" + existing.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaTypes.HAL_FORMS_JSON))
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

    assertEquals(updated.getId(), content.getId());

    verify(orderService, times(1)).findById(any(UUID.class));
    verify(orderService, times(1)).update(any(UUID.class), any(Order.class));
  }

  @Test
  void patchOrder_WhenValidStatusPatchProvided_ReturnsPatchedOrder() throws Exception {
    Order existing = Instancio.create(Order.class);
    Order updated = Instancio.create(Order.class);

    when(orderService.findById(any(UUID.class))).thenReturn(Optional.of(existing));
    when(orderService.update(any(UUID.class), any(Order.class))).thenReturn(Optional.of(updated));

    OrderStatusPatchOperation patchOp =
        new OrderStatusPatchOperation()
            .operation(OrderPatchRequestOperation.UPDATE_STATUS)
            .status(Instancio.create(OrderStatus.class));

    String requestJson = objectMapper.writeValueAsString(patchOp);

    String responseJson =
        mockMvc
            .perform(
                patch(ENDPOINT + "/" + existing.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaTypes.HAL_FORMS_JSON))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.createdDate").exists())
            .andExpect(jsonPath("$.lastModifiedDate").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

    EntityModel<it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order> response =
        objectMapper.readValue(responseJson, new TypeReference<>() {});

    Assertions.assertNotNull(response.getContent());
    assertEquals(updated.getId(), response.getContent().getId());

    verify(orderService, times(1)).findById(any(UUID.class));
    verify(orderService, times(1)).update(any(UUID.class), any(Order.class));
  }

  @Test
  void patchOrder_WhenOrderDoesNotExist_ReturnsNotFound() throws Exception {
    UUID randomId = UUID.randomUUID();

    when(orderService.findById(any(UUID.class))).thenReturn(Optional.empty());

    OrderDetailsPatchOperation patchOp = Instancio.create(OrderDetailsPatchOperation.class);

    String requestJson = objectMapper.writeValueAsString(patchOp);

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    patch(ENDPOINT + "/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").exists()));

    verify(orderService, times(1)).findById(any(UUID.class));
  }

  @Test
  void patchOrder_WhenInvalidRequestBody_ReturnsBadRequest() {
    String invalidJson = "{ invalid json }";
    UUID randomId = UUID.randomUUID();

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    patch(ENDPOINT + "/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest()));
  }

  @Test
  void patchOrder_WhenValidationFails_ReturnsUnprocessableEntity() throws Exception {
    Order existing = Instancio.create(Order.class);

    when(orderService.findById(any(UUID.class))).thenReturn(Optional.of(existing));
    when(orderService.update(any(UUID.class), any(Order.class)))
        .thenThrow(new IllegalArgumentException("Invalid patch data"));

    OrderDetailsPatchOperation patchOp = Instancio.create(OrderDetailsPatchOperation.class);

    String requestJson = objectMapper.writeValueAsString(patchOp);

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    patch(ENDPOINT + "/" + existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnprocessableEntity()));

    verify(orderService, times(1)).update(any(UUID.class), any(Order.class));
  }

  @Test
  void patchOrder_WhenDatabaseUnavailable_ReturnsInternalServerError() throws Exception {
    Order existing = Instancio.create(Order.class);

    when(orderService.findById(any(UUID.class))).thenReturn(Optional.of(existing));
    when(orderService.update(any(UUID.class), any(Order.class)))
        .thenThrow(new RuntimeException("DB unavailable"));

    OrderDetailsPatchOperation patchOp = Instancio.create(OrderDetailsPatchOperation.class);

    String requestJson = objectMapper.writeValueAsString(patchOp);

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    patch(ENDPOINT + "/" + existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").value("DB unavailable")));

    verify(orderService, times(1)).update(any(UUID.class), any(Order.class));
  }

  @Test
  void patchOrder_WhenOperationMissing_ReturnsBadRequest() {
    UUID randomId = UUID.randomUUID();

    String jsonMissingOperation =
        """
        {
          "details": { "description": "test" }
        }
        """;

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    patch(ENDPOINT + "/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMissingOperation))
                .andExpect(status().isBadRequest()));
  }

  @Test
  void patchOrder_WhenOperationNotRecognized_ReturnsBadRequest() {
    UUID randomId = UUID.randomUUID();

    String jsonInvalidOperation =
        """
        {
          "operation": "DO_NOT_EXIST",
          "details": { "description": "test" }
        }
        """;

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    patch(ENDPOINT + "/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalidOperation))
                .andExpect(status().isBadRequest()));
  }
}
