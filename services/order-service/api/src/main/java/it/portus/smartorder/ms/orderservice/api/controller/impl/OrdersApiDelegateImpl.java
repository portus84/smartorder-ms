package it.portus.smartorder.ms.orderservice.api.controller.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import it.portus.ms.commons.mappers.PageToPageMapper;
import it.portus.ms.commons.utils.PageableUtils;
import it.portus.smartorder.ms.orderservice.api.exception.OrderNotFoundException;
import it.portus.smartorder.ms.orderservice.api.hateoas.HateoasOrderHelper;
import it.portus.smartorder.ms.orderservice.api.mappers.OrderMapper;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.OrdersApiDelegate;
import it.portus.smartorder.ms.orderservice.api.v1.openapi.model.*;
import it.portus.smartorder.ms.orderservice.business.domain.model.QOrder;
import it.portus.smartorder.ms.orderservice.business.services.OrderService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;
import org.jspecify.annotations.Nullable;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class OrdersApiDelegateImpl implements OrdersApiDelegate {

  private final OrderService orderService;

  private final OrderMapper orderMapper;
  private final PageToPageMapper pageToPageMapper;

  private final HateoasOrderHelper hateoasHelper;

  @Override
  public ResponseEntity<PagedModel<EntityModel<Order>>> getOrders(
      Integer page, Integer size, List<String> sort, @Nullable OrderStatus status) {
    QOrder qOrder = QOrder.order;

    BooleanExpression basePredicate = qOrder.id.isNotNull();

    BooleanExpression predicate =
        Optional.ofNullable(status)
            .map(
                s ->
                    basePredicate.and(
                        qOrder.state.status.eq(
                            it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus
                                .valueOf(s.getValue()))))
            .orElse(basePredicate);

    return ResponseEntity.ok(
        hateoasHelper.toPagedModel(
            pageToPageMapper.toPage(
                orderService.findAll(predicate, PageableUtils.of(page, size, sort)), Order.class)));
  }

  @Override
  public ResponseEntity<EntityModel<Order>> getOrderById(String id) {
    return orderService
        .findById(new ObjectId(id))
        .map(orderMapper::toDTO)
        .map(hateoasHelper::toEntityModel)
        .map(ResponseEntity::ok)
        .orElseThrow(() -> new OrderNotFoundException(id));
  }

  @SneakyThrows
  @Override
  public ResponseEntity<EntityModel<Order>> addOrder(CreateOrderRequest request) {
    EntityModel<Order> createdEntity =
        hateoasHelper.toEntityModel(
            orderMapper.toDTO(orderService.save(orderMapper.toBO(request))));

    return ResponseEntity.created(
            UriComponentsBuilder.fromUriString(
                    createdEntity.getRequiredLink(IanaLinkRelations.SELF).getHref())
                .build()
                .toUri())
        .body(createdEntity);
  }

  @Override
  public ResponseEntity<EntityModel<Order>> updateOrder(
      String id, UpdateOrderRequest updateRequest) {
    return orderService
        .update(new ObjectId(id), orderMapper.toBO(updateRequest))
        .map(updatedEntity -> hateoasHelper.toEntityModel(orderMapper.toDTO(updatedEntity)))
        .map(ResponseEntity::ok)
        .orElseThrow(() -> new OrderNotFoundException(id));
  }

  @Override
  public ResponseEntity<Void> deleteOrder(String id) {
    return orderService
        .findById(new ObjectId(id))
        .map(
            existing -> {
              orderService.delete(existing);
              return ResponseEntity.noContent().<Void>build();
            })
        .orElseThrow(() -> new OrderNotFoundException(id));
  }

  @Override
  public ResponseEntity<EntityModel<Order>> patchOrder(String id, OrderPatchRequest patchRequest) {
    return orderService
        .findById(new ObjectId(id))
        .flatMap(
            existing -> orderService.update(existing.getId(), applyPatch(existing, patchRequest)))
        .map(
            updatedOrder ->
                ResponseEntity.ok(hateoasHelper.toEntityModel(orderMapper.toDTO(updatedOrder))))
        .orElseThrow(() -> new OrderNotFoundException(id));
  }

  private it.portus.smartorder.ms.orderservice.business.domain.model.Order applyPatch(
      it.portus.smartorder.ms.orderservice.business.domain.model.Order existing,
      OrderPatchRequest patchRequest) {
    return switch (patchRequest) {
      case OrderDetailsPatchOperation op ->
          existing.toBuilder().description(op.getDetails().getDescription()).build();
      case OrderStatusPatchOperation op ->
          existing.toBuilder()
              .state(
                  it.portus.smartorder.ms.orderservice.business.domain.model.OrderState.builder()
                      .status(
                          it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus
                              .valueOf(op.getStatus().getValue()))
                      .build())
              .build();
      default -> throw new IllegalStateException("Unexpected value: " + patchRequest);
    };
  }
}
