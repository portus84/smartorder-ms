package it.portus.smartorder.ms.orderservice.api.controllers.orders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.portus.smartorder.ms.orderservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.orderservice.api.controller.impl.OrdersApiDelegateImpl;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.OrdersApiController;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.model.UpdateOrderRequest;
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
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrdersApiController.class)
@Import({ControllerTestConfig.class, OrdersApiDelegateImpl.class})
@MockitoBean(types = {CacheManager.class})
class UpdateOrderControllerTest {

  private static final String ENDPOINT = "/api/v1/orders";

  @Autowired private MockMvc mockMvc;
  @MockitoBean private OrderService orderService;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void updateOrder_WhenValidOrderProvided_ReturnsUpdatedOrder() throws Exception {
    Order mockedOrder = Instancio.create(Order.class);

    when(orderService.update(any(UUID.class), any(Order.class)))
        .thenReturn(Optional.of(mockedOrder));

    String requestJson = objectMapper.writeValueAsString(buildUpdateOrderRequest());

    String responseJson =
        mockMvc
            .perform(
                put(ENDPOINT + "/" + mockedOrder.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
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

    verify(orderService, times(1)).update(any(UUID.class), any(Order.class));
  }

  @Test
  void updateOrder_WhenOrderDoesNotExist_ReturnsNotFound() throws Exception {
    UUID randomId = UUID.randomUUID();

    when(orderService.update(any(), any())).thenReturn(Optional.empty());

    String requestJson = objectMapper.writeValueAsString(buildUpdateOrderRequest());

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    put(ENDPOINT + "/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").exists()));

    verify(orderService, times(1)).update(any(UUID.class), any(Order.class));
  }

  @Test
  void updateOrder_WhenInvalidRequestBody_ReturnsBadRequest() {
    String invalidRequestJson = "{ invalid json }";
    UUID randomId = UUID.randomUUID();

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    put(ENDPOINT + "/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest()));
  }

  @Test
  void updateOrder_WhenValidationFails_ReturnsUnprocessableEntity() throws Exception {
    UUID randomId = UUID.randomUUID();

    when(orderService.update(any(UUID.class), any(Order.class)))
        .thenThrow(new IllegalArgumentException("Invalid data"));

    String requestJson = objectMapper.writeValueAsString(buildUpdateOrderRequest());

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    put(ENDPOINT + "/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnprocessableEntity()));

    verify(orderService, times(1)).update(any(UUID.class), any(Order.class));
  }

  @Test
  void updateOrder_WhenDatabaseUnavailable_ReturnsInternalServerError() throws Exception {
    UUID randomId = UUID.randomUUID();

    when(orderService.update(any(UUID.class), any(Order.class)))
        .thenThrow(new RuntimeException("DB unavailable"));

    String requestJson = objectMapper.writeValueAsString(buildUpdateOrderRequest());

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    put(ENDPOINT + "/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").value("DB unavailable")));

    verify(orderService, times(1)).update(any(UUID.class), any(Order.class));
  }

  private UpdateOrderRequest buildUpdateOrderRequest() {
    return Instancio.create(UpdateOrderRequest.class);
  }
}
