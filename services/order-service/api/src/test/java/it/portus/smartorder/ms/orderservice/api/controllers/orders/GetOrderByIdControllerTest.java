package it.portus.smartorder.ms.orderservice.api.controllers.orders;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.portus.smartorder.ms.orderservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.orderservice.api.controller.impl.OrdersApiDelegateImpl;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.OrdersApiController;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.services.OrderService;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrdersApiController.class)
@Import({ControllerTestConfig.class, OrdersApiDelegateImpl.class})
@MockitoBean(types = {CacheManager.class})
class GetOrderByIdControllerTest {

  private static final String ENDPOINT = "/api/v1/orders/{id}";

  @Autowired private MockMvc mockMvc;
  @MockitoBean private OrderService orderService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void getOrderById_WhenOrderExists_ReturnsOrder() throws Exception {
    Order mocked = Instancio.create(Order.class);
    when(orderService.findById(mocked.getId())).thenReturn(Optional.of(mocked));

    String responseJson =
        mockMvc
            .perform(get(ENDPOINT, mocked.getId()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    EntityModel<it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order> response =
        objectMapper.readValue(responseJson, new TypeReference<>() {});

    Assertions.assertNotNull(response.getContent());
    assertEquals(mocked.getId(), response.getContent().getId());

    verify(orderService, times(1)).findById(mocked.getId());
  }

  @Test
  void getOrderById_WhenOrderNotFound_ReturnsNotFound() {
    UUID id = UUID.randomUUID();

    when(orderService.findById(id)).thenReturn(Optional.empty());

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(get(ENDPOINT, id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").exists()));
  }

  @Test
  void getOrderById_WhenInvalidParameterPassed_ReturnsBadRequest() {
    assertDoesNotThrow(
        () -> mockMvc.perform(get(ENDPOINT, "bad-id")).andExpect(status().isBadRequest()));
  }

  @Test
  void getOrderById_WhenInvalidParameterPassed_ReturnsUnprocessableEntity() {
    when(orderService.findById(any(UUID.class)))
        .thenThrow(new IllegalArgumentException("Invalid parameter"));

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(get(ENDPOINT, UUID.randomUUID()))
                .andExpect(status().isUnprocessableEntity()));
  }

  @Test
  void getOrderById_WhenDatabaseUnavailable_ReturnsInternalServerErrorWithDetails() {
    when(orderService.findById(any(UUID.class))).thenThrow(new RuntimeException("DB unavailable"));

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(get(ENDPOINT, UUID.randomUUID()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").value("DB unavailable")));
  }

  @Test
  @Disabled("Authorization not implemented yet")
  void getOrderById_WhenUnauthorized_ReturnsUnauthorized() {
    assertDoesNotThrow(
        () -> mockMvc.perform(get(ENDPOINT, "anyId")).andExpect(status().isUnauthorized()));
  }
}
