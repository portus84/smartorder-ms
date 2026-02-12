package it.portus.smartorder.ms.orderservice.api.controllers.orders;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Predicate;
import it.portus.ms.commons.mappers.PageToPagedModelMapper;
import it.portus.smartorder.ms.orderservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.orderservice.api.controller.impl.OrdersApiDelegateImpl;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.OrdersApiController;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderState;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import it.portus.smartorder.ms.orderservice.business.services.OrderService;
import java.util.List;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(controllers = OrdersApiController.class)
@Import({ControllerTestConfig.class, OrdersApiDelegateImpl.class})
@MockitoBean(types = {CacheManager.class})
class GetOrdersControllerTest {

  private static final String ENDPOINT = "/api/v1/orders";

  @Autowired private MockMvc mockMvc;
  @MockitoBean private OrderService orderService;

  @Autowired private ObjectMapper objectMapper;
  @Autowired private PageToPagedModelMapper pageToPagedModelMapper;

  @Test
  void getOrders_WhenOrdersExist_ReturnsAllOrders() throws Exception {
    List<Order> content = getMockedOrders();
    int contentSize = content.size();

    PageImpl<Order> page = new PageImpl<>(content, PageRequest.of(0, contentSize), contentSize);
    when(orderService.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

    String responseJson =
        mockMvc
            .perform(get(ENDPOINT))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.orders", hasSize(contentSize)))
            .andExpect(jsonPath("$.page.totalElements", equalTo(contentSize)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    PagedModel<EntityModel<it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order>>
        responsePage = objectMapper.readValue(responseJson, new TypeReference<>() {});

    PagedModel<EntityModel<it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order>>
        expectedPage =
            pageToPagedModelMapper.toPagedModel(
                page, it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order.class);

    assertEquals(expectedPage.getMetadata(), responsePage.getMetadata());
    assertThat(expectedPage.getContent())
        .usingRecursiveComparison()
        .ignoringFields("links")
        .isEqualTo(responsePage.getContent());

    verify(orderService, times(1)).findAll(any(Predicate.class), any(Pageable.class));
  }

  @Test
  void getOrders_WhenQueryParametersProvided_ReturnsSortedPagedOrders() throws Exception {
    List<Order> content = getMockedOrders();
    int contentSize = content.size();

    PageRequest pageRequest =
        PageRequest.of(1, 5, Sort.by(List.of(Sort.Order.by("id").with(Sort.Direction.DESC))));

    when(orderService.findAll(any(Predicate.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(content, pageRequest, contentSize));

    MockHttpServletRequestBuilder requestBuilder =
        get(ENDPOINT)
            .param("page", String.valueOf(pageRequest.getPageNumber()))
            .param("size", String.valueOf(pageRequest.getPageSize()));

    pageRequest
        .getSort()
        .forEach(
            o -> {
              String sortParam = o.getProperty() + "," + o.getDirection().name().toLowerCase();
              requestBuilder.param("sort", sortParam);
            });

    mockMvc
        .perform(requestBuilder)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.orders", hasSize(contentSize)));

    verify(orderService, times(1)).findAll(any(Predicate.class), any(Pageable.class));
  }

  @Test
  void getOrders_WhenStatusQueryParamProvided_FiltersByStatus() throws Exception {
    OrderStatus statusToFilter = OrderStatus.DELIVERED;

    List<Order> content = getMockedOrders(statusToFilter);
    int contentSize = content.size();

    PageImpl<Order> page = new PageImpl<>(content, PageRequest.of(0, contentSize), contentSize);

    when(orderService.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

    String responseJson =
        mockMvc
            .perform(
                get(ENDPOINT)
                    .param(
                        "status",
                        it.portus.smartorder.ms.orderservice.api.v1.openapi.model.OrderStatus
                            .valueOf(statusToFilter.name())
                            .getValue()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.orders", hasSize(contentSize)))
            .andExpect(jsonPath("$.page.totalElements", equalTo(contentSize)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    PagedModel<EntityModel<it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order>>
        responsePage = objectMapper.readValue(responseJson, new TypeReference<>() {});

    PagedModel<EntityModel<it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order>>
        expectedPage =
            pageToPagedModelMapper.toPagedModel(
                page, it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order.class);

    assertEquals(expectedPage.getMetadata(), responsePage.getMetadata());
    assertThat(expectedPage.getContent())
        .usingRecursiveComparison()
        .ignoringFields("links")
        .isEqualTo(responsePage.getContent());

    verify(orderService, times(1)).findAll(any(Predicate.class), any(Pageable.class));
  }

  @Test
  void getOrders_WhenPageNumberIsNegative_ReturnsBadRequest() {
    assertDoesNotThrow(
        () ->
            mockMvc.perform(get(ENDPOINT).param("page", "-1")).andExpect(status().isBadRequest()));
  }

  @Test
  @Disabled("Authorization not implemented yet")
  void getOrders_WhenUnauthorized_ReturnsUnauthorized() {
    assertDoesNotThrow(() -> mockMvc.perform(get(ENDPOINT)).andExpect(status().isUnauthorized()));
  }

  @Test
  void getOrders_WhenInvalidParameterPassed_ReturnsUnprocessableEntity() {
    when(orderService.findAll(any(Predicate.class), any(Pageable.class)))
        .thenThrow(new IllegalArgumentException("Invalid parameter"));

    assertDoesNotThrow(
        () -> mockMvc.perform(get(ENDPOINT)).andExpect(status().isUnprocessableEntity()));
  }

  @Test
  void getOrders_WhenDatabaseUnavailable_ReturnsInternalServerErrorWithDetails() {
    when(orderService.findAll(any(Predicate.class), any(Pageable.class)))
        .thenThrow(new RuntimeException("DB unavailable"));

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(get(ENDPOINT))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").value("DB unavailable")));
  }

  private static List<Order> getMockedOrders() {
    return Instancio.ofList(Order.class).size(5).create();
  }

  private static List<Order> getMockedOrders(OrderStatus status) {
    return Instancio.ofList(Order.class)
        .size(5)
        .set(Select.field(OrderState::getStatus).within(Select.scope(OrderState.class)), status)
        .create();
  }
}
