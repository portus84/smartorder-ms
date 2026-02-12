package it.portus.smartorder.ms.orderservice.api.controllers.orders;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import it.portus.smartorder.ms.orderservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.orderservice.api.controller.impl.OrdersApiDelegateImpl;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.OrdersApiController;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.services.OrderService;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.types.ObjectId;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrdersApiController.class)
@Import({ControllerTestConfig.class, OrdersApiDelegateImpl.class})
@MockitoBean(types = {CacheManager.class})
class DeleteOrderControllerTest {

  private static final String ENDPOINT = "/api/v1/orders/{id}";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private OrderService orderService;

  @Test
  void deleteOrder_WhenOrderExists_ReturnsNoContent() {
    ObjectId id = new ObjectId(RandomStringUtils.secure().nextNumeric(24));

    when(orderService.findById(id)).thenReturn(Optional.of(Instancio.create(Order.class)));
    doNothing().when(orderService).deleteById(id);

    assertDoesNotThrow(
        () ->
            mockMvc.perform(delete(ENDPOINT, id.toHexString())).andExpect(status().isNoContent()));
  }

  @Test
  void deleteOrder_WhenOrderNotFound_ReturnsNotFound() {
    ObjectId id = new ObjectId(RandomStringUtils.secure().nextNumeric(24));

    when(orderService.findById(id)).thenReturn(Optional.empty());

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(delete(ENDPOINT, id.toHexString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").exists()));
  }

  @Test
  void deleteOrder_WhenInvalidIdPassed_ReturnsBadRequest() {
    assertDoesNotThrow(
        () -> mockMvc.perform(delete(ENDPOINT, "bad-id")).andExpect(status().isBadRequest()));
  }

  @Test
  void deleteOrder_WhenServiceThrowsIllegalArgument_ReturnsUnprocessableEntity() {
    when(orderService.findById(any(ObjectId.class)))
        .thenThrow(new IllegalArgumentException("Invalid parameter"));

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(delete(ENDPOINT, RandomStringUtils.secure().nextNumeric(24)))
                .andExpect(status().isUnprocessableEntity()));
  }

  @Test
  void deleteOrder_WhenDatabaseUnavailable_ReturnsInternalServerErrorWithDetails() {
    when(orderService.findById(any(ObjectId.class)))
        .thenThrow(new RuntimeException("DB unavailable"));

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(delete(ENDPOINT, RandomStringUtils.secure().nextNumeric(24)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").value("DB unavailable")));
  }
}
