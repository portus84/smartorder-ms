package it.portus.smartorder.ms.orderservice.api.controllers.orders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import it.portus.ms.test.controller.AbstractControllerTest;
import it.portus.smartorder.ms.orderservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.orderservice.api.controller.impl.OrdersApiDelegateImpl;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.OrdersApiController;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.model.CreateOrderRequest;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.services.OrderService;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = OrdersApiController.class)
@Import({ControllerTestConfig.class, OrdersApiDelegateImpl.class})
@MockitoBean(types = {CacheManager.class})
class AddOrderControllerTest extends AbstractControllerTest {

  private static final String ENDPOINT = "/api/v1/orders";

  @MockitoBean private OrderService orderService;

  @Override
  protected String endpoint() {
    return ENDPOINT;
  }

  @Test
  @SneakyThrows
  void addOrder_WhenValidOrderProvided_ReturnsCreatedOrder() {
    Order mocked = Instancio.create(Order.class);

    when(orderService.save(any(Order.class))).thenReturn(mocked);

    String responseJson =
        post(Instancio.create(CreateOrderRequest.class))
            .andExpect(status().isCreated())
            .andExpect(header().exists(HttpHeaders.LOCATION))
            .andExpect(
                header()
                    .string(
                        HttpHeaders.LOCATION,
                        org.hamcrest.Matchers.containsString(mocked.getId().toString())))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.createdDate").exists())
            .andExpect(jsonPath("$.lastModifiedDate").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

    EntityModel<it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order> response =
        objectMapper.readValue(responseJson, new TypeReference<>() {});

    assertNotNull(response);
    it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order content = response.getContent();

    Assertions.assertNotNull(content);
    assertEquals(mocked.getId(), content.getId());
    assertTrue(response.getLink(IanaLinkRelations.SELF.value()).isPresent());

    verify(orderService).save(any(Order.class));
  }

  @ParameterizedTest(name = "{0} => BadRequest")
  @ValueSource(strings = {"{ invalid json }", "{ }"})
  @SneakyThrows
  void addOrder_WhenInvalidRequestBody_ReturnsBadRequest(String body) {
    post(body).andExpect(errorResponse(HttpStatus.BAD_REQUEST));
  }

  @Test
  @SneakyThrows
  void addOrder_WhenInvalidParameterPassed_ReturnsUnprocessableEntity() {
    String message = "Invalid data";
    when(orderService.save(any(Order.class))).thenThrow(new IllegalArgumentException(message));

    post(Instancio.create(CreateOrderRequest.class))
        .andExpect(errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, message));
  }

  @Test
  @SneakyThrows
  void addOrder_WhenDatabaseUnavailable_ReturnsInternalServerErrorWithDetails() {
    String message = "DB unavailable";
    when(orderService.save(any(Order.class))).thenThrow(new RuntimeException(message));

    post(Instancio.create(CreateOrderRequest.class))
        .andExpect(errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message));
  }

  @Test
  @SneakyThrows
  @Disabled("Authorization not implemented yet")
  void addOrder_WhenUnauthorized_ReturnsUnauthorized() {
    post("{}").andExpect(status().isUnauthorized());
  }
}
