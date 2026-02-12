package it.portus.smartorder.ms.orderservice.business.services.impl;

import com.querydsl.core.types.Predicate;
import it.portus.business.commons.service.MongoCrudService;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import it.portus.smartorder.ms.orderservice.business.domain.repositories.OrderRepository;
import it.portus.smartorder.ms.orderservice.business.rules.order.OrderRuleEngine;
import it.portus.smartorder.ms.orderservice.business.services.OrderService;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends MongoCrudService<Order, ObjectId> implements OrderService {

  private static final class CacheNames {
    public static final String ORDERS = "orders";
    public static final String ORDERS_ALL = "orders_all";
  }

  @Nullable private final OrderRuleEngine rulesService;

  public OrderServiceImpl(
      OrderRepository repository, @Autowired(required = false) OrderRuleEngine rulesService) {
    super(repository);
    this.rulesService = rulesService;
  }

  @Override
  @Caching(
      put = {@CachePut(value = CacheNames.ORDERS, key = "#result.id.toHexString()")},
      evict = {@CacheEvict(value = CacheNames.ORDERS_ALL, allEntries = true)})
  @SuppressWarnings("unchecked")
  public <S extends Order> S save(S entity) {
    return super.save((S) applyRules(entity));
  }

  @Override
  @Caching(
      put = {
        @CachePut(
            value = CacheNames.ORDERS,
            key = "#result.id.toHexString()",
            condition = "#result != null")
      },
      evict = {@CacheEvict(value = CacheNames.ORDERS_ALL, allEntries = true)})
  public <S extends Order> Optional<S> update(ObjectId id, S entity) {
    return super.findById(id)
        .flatMap(
            existing -> {
              applyRules(applyStatusTransition(existing, entity.getState().getStatus()));
              return super.update(existing.getId(), entity);
            });
  }

  @Override
  @CacheEvict(value = CacheNames.ORDERS_ALL, allEntries = true)
  public <S extends Order> Iterable<S> saveAll(Iterable<S> entities) {
    return super.saveAll(entities);
  }

  @Override
  @Cacheable(value = CacheNames.ORDERS, key = "#p0.toHexString()")
  public Optional<Order> findById(ObjectId objectId) {
    return super.findById(objectId);
  }

  @Override
  @Cacheable(
      value = CacheNames.ORDERS_ALL,
      key = "#p0 != null ? #p0.pageNumber + '-' + #p0.pageSize : 'unpaged'")
  public Page<Order> findAll(Pageable pageable) {
    return super.findAll(pageable);
  }

  @Override
  @Cacheable(
      value = CacheNames.ORDERS_ALL,
      key =
          "#p1 != null ? #p1.pageNumber + '-' + #p1.pageSize + '-' + (#p0.toString().hashCode() + '-' + #p0.toString()) : 'unpaged-' + (#p0.toString().hashCode() + '-' + #p0.toString())")
  public Page<Order> findAll(Predicate predicate, Pageable pageable) {
    return ((OrderRepository) repository).findAll(predicate, pageable);
  }

  @Override
  @Caching(
      evict = {
        @CacheEvict(value = CacheNames.ORDERS, key = "#p0.toHexString()"),
        @CacheEvict(value = CacheNames.ORDERS_ALL, allEntries = true)
      })
  public void deleteById(ObjectId objectId) {
    super.deleteById(objectId);
  }

  @Override
  @Caching(
      evict = {
        @CacheEvict(value = CacheNames.ORDERS, key = "#p0.id.toHexString()"),
        @CacheEvict(value = CacheNames.ORDERS_ALL, allEntries = true)
      })
  public void delete(Order entity) {
    super.delete(applyRules(entity));
  }

  @Override
  public List<OrderStatus> getTransitionStatuses(Order order) {
    return Optional.ofNullable(rulesService)
        .map(r -> r.getTransitionStatuses(order))
        .orElse(List.of());
  }

  private Order applyRules(Order order) {
    return Optional.ofNullable(rulesService).map(r -> r.applyRules(order)).orElse(order);
  }

  private Order applyStatusTransition(Order existing, OrderStatus newStatus) {
    return Optional.ofNullable(rulesService)
        .map(r -> r.applyStatusTransition(existing, newStatus))
        .orElse(existing);
  }
}
