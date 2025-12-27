package it.portus.smartorder.ms.orderservice.business.services;

import com.querydsl.core.types.Predicate;
import it.portus.business.commons.service.CrudService;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService extends CrudService<Order, ObjectId> {

  Iterable<Order> findAll(Predicate predicate);

  Page<Order> findAll(Predicate predicate, Pageable pageable);

  List<OrderStatus> getTransitionStatuses(Order order);
}
