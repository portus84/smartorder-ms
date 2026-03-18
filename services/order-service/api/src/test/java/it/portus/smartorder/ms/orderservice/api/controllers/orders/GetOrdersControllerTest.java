package it.portus.smartorder.ms.orderservice.api.controllers.orders;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.querydsl.core.types.Predicate;
import it.portus.ms.commons.mappers.PageToPagedModelMapper;
import it.portus.ms.test.controller.AbstractControllerTest;
import it.portus.smartorder.ms.orderservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.orderservice.api.controller.impl.OrdersApiDelegateImpl;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.OrdersApiController;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderState;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import it.portus.smartorder.ms.orderservice.business.services.OrderService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = OrdersApiController.class)
@Import({ControllerTestConfig.class, OrdersApiDelegateImpl.class})
@MockitoBean(types = {CacheManager.class})
class GetOrdersControllerTest extends AbstractControllerTest {

  private static final String ENDPOINT = "/api/v1/orders";

  @MockitoBean private OrderService orderService;

  @Autowired private PageToPagedModelMapper pageToPagedModelMapper;

  @Override
  protected String endpoint() {
    return ENDPOINT;
  }

  @Test
  @SneakyThrows
  void getOrders_WhenOrdersExist_ReturnsAllOrders() {
    List<Order> content = getMockedOrders();
    int contentSize = content.size();

    PageImpl<Order> page = new PageImpl<>(content, PageRequest.of(0, contentSize), contentSize);
    when(orderService.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

    String responseJson =
        get()
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
    assertPageEquals(expectedPage.getContent(), responsePage.getContent());

    verify(orderService).findAll(any(Predicate.class), any(Pageable.class));
  }

  @Test
  @SneakyThrows
  void getOrders_WhenQueryParametersProvided_ReturnsSortedPagedOrders() {
    List<Order> content = getMockedOrders();
    int contentSize = content.size();

    PageRequest pageRequest =
        PageRequest.of(1, 5, Sort.by(List.of(Sort.Order.by("id").with(Sort.Direction.DESC))));

    when(orderService.findAll(any(Predicate.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(content, pageRequest, contentSize));

    getWithPageRequest(pageRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.orders", hasSize(contentSize)));

    verify(orderService).findAll(any(Predicate.class), any(Pageable.class));
  }

  @Test
  @SneakyThrows
  void getOrders_WhenStatusQueryParamProvided_FiltersByStatus() {
    OrderStatus statusToFilter = OrderStatus.DELIVERED;

    List<Order> content = getMockedOrders(statusToFilter);
    int contentSize = content.size();

    PageImpl<Order> page = new PageImpl<>(content, PageRequest.of(0, contentSize), contentSize);

    when(orderService.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

    String responseJson =
        getWithParams(
                Map.of(
                    "status",
                    it.portus.smartorder.ms.orderservice.api.v1.openapi.model.OrderStatus.valueOf(
                            statusToFilter.name())
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
    assertPageEquals(expectedPage.getContent(), responsePage.getContent());

    verify(orderService).findAll(any(Predicate.class), any(Pageable.class));
  }

  @Test
  @SneakyThrows
  void getOrders_WhenPageNumberIsNegative_ReturnsBadRequest() {
    getWithParams(Map.of("page", "-1")).andExpect(errorResponse(HttpStatus.BAD_REQUEST));
  }

  @Test
  @SneakyThrows
  @Disabled("Authorization not implemented yet")
  void getOrders_WhenUnauthorized_ReturnsUnauthorized() {
    get().andExpect(status().isUnauthorized());
  }

  @Test
  @SneakyThrows
  void getOrders_WhenInvalidParameterPassed_ReturnsUnprocessableEntity() {
    String message = "Invalid parameter";
    when(orderService.findAll(any(Predicate.class), any(Pageable.class)))
        .thenThrow(new IllegalArgumentException(message));

    get().andExpect(errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, message));
  }

  @Test
  @SneakyThrows
  void getOrders_WhenDatabaseUnavailable_ReturnsInternalServerErrorWithDetails() {
    String message = "DB unavailable";
    when(orderService.findAll(any(Predicate.class), any(Pageable.class)))
        .thenThrow(new RuntimeException(message));

    get().andExpect(errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message));
  }

  private <T> void assertPageEquals(
      Collection<EntityModel<T>> expected, Collection<EntityModel<T>> actual) {
    JsonNode expectedNode = objectMapper.valueToTree(expected);
    JsonNode actualNode = objectMapper.valueToTree(actual);

    expectedNode.forEach(n -> ((ObjectNode) n).remove("links"));
    actualNode.forEach(n -> ((ObjectNode) n).remove("links"));

    assertEquals(expectedNode, actualNode);
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
