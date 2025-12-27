package it.portus.smartorder.ms.orderservice.api.hateoas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import it.portus.smartorder.ms.orderservice.api.mappers.OrderMapper;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order;
import it.portus.smartorder.ms.orderservice.business.services.OrderService;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.RepresentationModelAssembler;

@ExtendWith(MockitoExtension.class)
class HateoasOrderHelperTest {

  @Mock private PagedResourcesAssembler<Order> pagedResourcesAssembler;
  @Mock private Environment environment;
  @Mock private OrderService orderService;
  @Mock private OrderMapper orderMapper;

  private HateoasOrderHelper helper;

  @BeforeEach
  void setUp() {
    when(environment.getProperty(anyString())).thenReturn(null);
    when(orderService.getTransitionStatuses(any())).thenReturn(List.of());

    helper =
        new HateoasOrderHelper(environment, pagedResourcesAssembler, orderService, orderMapper);
  }

  @Test
  void toEntityModel_ValidOrder_ReturnsEntityModelWithSelfLink() {
    Order order = Instancio.of(Order.class).create();
    var orderBO =
        Mockito.mock(it.portus.smartorder.ms.orderservice.business.domain.model.Order.class);
    when(orderMapper.toBO(order)).thenReturn(orderBO);
    when(orderService.getTransitionStatuses(orderBO)).thenReturn(List.of());

    EntityModel<Order> model = helper.toEntityModel(order);

    assertNotNull(model);
    Order content = model.getContent();
    assertNotNull(content);

    assertThat(content).isEqualTo(order);

    Link actualSelf = model.getRequiredLink(IanaLinkRelations.SELF);
    assertNotNull(actualSelf);
    assertThat(actualSelf.getRel()).isEqualTo(IanaLinkRelations.SELF);
    assertThat(actualSelf.getHref()).contains(String.format("/orders/%s", order.getId()));
  }

  @Test
  void toPagedModel_PageOfOrders_ReturnsPagedModel() {
    List<Order> orders = Instancio.ofList(Order.class).size(2).create();
    var page = new PageImpl<>(orders);

    PagedModel<EntityModel<Order>> expectedPagedModel =
        PagedModel.of(
            orders.stream().map(helper::toEntityModel).toList(),
            new PagedModel.PageMetadata(orders.size(), 0, orders.size()));

    when(pagedResourcesAssembler.toModel(
            eq(page),
            ArgumentMatchers.<RepresentationModelAssembler<Order, EntityModel<Order>>>any()))
        .thenReturn(expectedPagedModel);

    PagedModel<EntityModel<Order>> result = helper.toPagedModel(page);

    Collection<EntityModel<Order>> content = result.getContent();

    assertNotNull(content);
    assertThat(content).hasSize(orders.size());

    assertThat(
            content.stream()
                .map(EntityModel::getContent)
                .filter(Objects::nonNull)
                .map(Order::getId))
        .containsExactlyElementsOf(orders.stream().map(Order::getId).toList());
  }
}
