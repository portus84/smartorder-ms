package it.portus.smartorder.ms.orderservice.api.controllers.orders;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.portus.smartorder.ms.orderservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.orderservice.api.controller.impl.OrdersApiDelegateImpl;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.OrdersApiController;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.model.CreateOrderRequest;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.services.OrderService;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrdersApiController.class)
@Import({ControllerTestConfig.class, OrdersApiDelegateImpl.class})
@MockitoBean(types = {CacheManager.class})
class AddOrderControllerTest {

  private static final String ENDPOINT = "/api/v1/orders";

  @Autowired private MockMvc mockMvc;
  @MockitoBean private OrderService orderService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void addOrder_WhenValidOrderProvided_ReturnsCreatedOrder() throws Exception {
    Order mocked = Instancio.create(Order.class);

    when(orderService.save(any(Order.class))).thenReturn(mocked);

    String requestJson = objectMapper.writeValueAsString(buildCreateOrderRequest());

    String responseJson =
        mockMvc
            .perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(requestJson))
            .andExpect(status().isCreated())
            .andExpect(header().exists(HttpHeaders.LOCATION))
            .andExpect(
                header()
                    .string(
                        HttpHeaders.LOCATION,
                        org.hamcrest.Matchers.containsString(mocked.getId().toHexString())))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.createdDate").exists())
            .andExpect(jsonPath("$.lastModifiedDate").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

    EntityModel<it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order> response =
        objectMapper.readValue(responseJson, new TypeReference<>() {});

    assertThat(response).isNotNull();
    it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order content = response.getContent();

    Assertions.assertNotNull(content);
    assertThat(content.getId()).isEqualTo(mocked.getId().toHexString());
    assertThat(response.getLink(IanaLinkRelations.SELF.value())).isPresent();

    verify(orderService, times(1)).save(any(Order.class));
  }

  @Test
  void addOrder_WhenInvalidRequestBody_ReturnsBadRequest() {
    String invalidRequestJson = "{ invalid json }";

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest()));
  }

  @Test
  void addOrder_WhenRequiredFieldMissing_ReturnsBadRequest() {
    String requestWithMissingField = "{}";

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestWithMissingField))
                .andExpect(status().isBadRequest()));
  }

  @Test
  void addOrder_WhenInvalidParameterPassed_ReturnsUnprocessableEntity() {
    when(orderService.save(any(Order.class)))
        .thenThrow(new IllegalArgumentException("Invalid data"));

    CreateOrderRequest createRequest = buildCreateOrderRequest();

    assertDoesNotThrow(
        () -> {
          String requestJson = objectMapper.writeValueAsString(createRequest);
          mockMvc
              .perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(requestJson))
              .andExpect(status().isUnprocessableEntity());
        });

    verify(orderService, times(1)).save(any(Order.class));
  }

  @Test
  void addOrder_WhenDatabaseUnavailable_ReturnsInternalServerErrorWithDetails() {
    when(orderService.save(any(Order.class))).thenThrow(new RuntimeException("DB unavailable"));

    CreateOrderRequest createRequest = buildCreateOrderRequest();

    assertDoesNotThrow(
        () -> {
          String requestJson = objectMapper.writeValueAsString(createRequest);
          mockMvc
              .perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(requestJson))
              .andExpect(status().isInternalServerError())
              .andExpect(jsonPath("$.errorCode").exists())
              .andExpect(jsonPath("$.errorMessage").exists())
              .andExpect(jsonPath("$.detailMessage").value("DB unavailable"));
        });

    verify(orderService, times(1)).save(any(Order.class));
  }

  @Test
  @Disabled("Authorization not implemented yet")
  void addOrder_WhenUnauthorized_ReturnsUnauthorized() {
    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized()));
  }

  private CreateOrderRequest buildCreateOrderRequest() {
    return Instancio.create(CreateOrderRequest.class);
  }
}
