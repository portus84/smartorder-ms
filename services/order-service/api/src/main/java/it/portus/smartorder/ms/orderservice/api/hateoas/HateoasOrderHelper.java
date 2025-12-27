package it.portus.smartorder.ms.orderservice.api.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import it.portus.ms.commons.hateoas.utils.HATEOASLinkUtils;
import it.portus.smartorder.ms.orderservice.api.mappers.OrderMapper;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.OrdersApiController;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.model.*;
import it.portus.smartorder.ms.orderservice.business.services.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HateoasOrderHelper {

  private static final Class<OrdersApiController> CONTROLLER_CLAZZ = OrdersApiController.class;

  private final Environment environment;

  private final PagedResourcesAssembler<Order> pagedResourcesAssembler;

  private final OrderService orderService;
  private final OrderMapper orderMapper;

  @SneakyThrows
  public EntityModel<Order> toEntityModel(Order entity) {
    Link self = buildSelfLink(entity);

    return EntityModel.of(
        entity,
        HATEOASLinkUtils.buildLinks(
            CONTROLLER_CLAZZ,
            environment,
            self.andAffordance(
                    afford(
                        WebMvcLinkBuilder.afford(
                            methodOn(CONTROLLER_CLAZZ).deleteOrder(entity.getId()))))
                .andAffordance(
                    afford(
                        WebMvcLinkBuilder.afford(
                            methodOn(CONTROLLER_CLAZZ).updateOrder(entity.getId(), null))))
                .andAffordances(buildUpdateAffordances(self))));
  }

  public PagedModel<EntityModel<Order>> toPagedModel(Page<Order> page) {
    return pagedResourcesAssembler.toModel(page, this::toEntityModel);
  }

  private Link buildSelfLink(Order entity) {
    List<String> allowedStatuses =
        orderService.getTransitionStatuses(orderMapper.toBO(entity)).stream()
            .map(Enum::name)
            .toList();

    return WebMvcLinkBuilder.linkTo(methodOn(CONTROLLER_CLAZZ).getOrderById(entity.getId()))
        .withSelfRel()
        .withTitle("Next allowed status values: " + StringUtils.join(allowedStatuses, ", "));
  }

  private List<Affordance> buildUpdateAffordances(Link self) {
    return Affordances.of(self)
        .afford(HttpMethod.PATCH)
        .withName("updateDetails")
        .withInput(OrderDetailsPatchOperation.class)
        .andAfford(HttpMethod.PATCH)
        .withName("updateStatus")
        .withInput(OrderStatusPatchOperation.class)
        .build()
        .stream()
        .map(this::afford)
        .toList();
  }

  private Affordance afford(Affordance affordance) {
    return HATEOASLinkUtils.buildAffordance(CONTROLLER_CLAZZ, environment, affordance);
  }
}
