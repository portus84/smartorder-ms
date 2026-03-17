package it.portus.smartorder.ms.orderservice.business.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import it.portus.ms.test.annotation.WithTestData;
import it.portus.smartorder.ms.orderservice.business.conf.MongoAutoConfiguration;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import it.portus.smartorder.ms.orderservice.business.domain.repositories.OrderRepository;
import it.portus.smartorder.ms.orderservice.business.loader.OrderTestDataLoader;
import it.portus.smartorder.ms.orderservice.business.rules.order.OrderRuleEngine;
import it.portus.smartorder.ms.orderservice.business.services.impl.OrderServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataMongoTest
@WithTestData(OrderTestDataLoader.class)
@ImportAutoConfiguration(MongoAutoConfiguration.class)
@Import({OrderServiceImpl.class})
class OrderServiceTest {

  @Autowired private OrderRepository orderRepository;

  @Autowired private OrderService orderService;

  @MockitoBean private OrderRuleEngine rulesService;

  @BeforeEach
  void setupMocks() {
    when(rulesService.applyRules(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    when(rulesService.applyStatusTransition(any(Order.class), any(OrderStatus.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void findAll_withOrders_returnsNonEmptyPage() {
    Page<Order> page = orderService.findAll(Pageable.unpaged());

    assertNotNull(page);
    assertFalse(page.isEmpty());
    assertTrue(page.getTotalElements() > 0);
  }

  @Test
  void findAll_noOrders_returnsEmptyPage() {
    orderRepository.deleteAll();

    Page<Order> page = orderService.findAll(Pageable.unpaged());

    assertNotNull(page);
    assertTrue(page.isEmpty());
    assertEquals(0, page.getTotalElements());
  }

  @Test
  void findById_existingOrder_returnsOrder() {
    Order existing = Instancio.ofList(Order.class).size(1).create().getFirst();
    existing = orderRepository.save(existing);

    Optional<Order> result = orderService.findById(existing.getId());

    assertTrue(result.isPresent());
    assertEquals(existing.getId(), result.get().getId());
  }

  @Test
  void findById_nonExistingOrder_returnsEmpty() {
    Optional<Order> result = orderService.findById(UUID.randomUUID());

    assertFalse(result.isPresent());
  }

  @Test
  void save_newOrder_createsOrderWithId() {
    Order order = Instancio.of(Order.class).create();

    Order saved = orderService.save(order);

    assertNotNull(saved);
    assertNotNull(saved.getId());
  }

  @Test
  void save_existingOrder_updatesOrder() {
    Order order = Instancio.of(Order.class).create();
    order = orderRepository.save(order);

    order.setDescription("Updated Description");
    Optional<Order> updated = orderService.update(order.getId(), order);

    assertTrue(updated.isPresent());
    assertEquals(order.getId(), updated.get().getId());
    assertEquals("Updated Description", updated.get().getDescription());
  }

  @Test
  void delete_existingOrder_logicallyRemovesOrder() {
    Order existing = Instancio.of(Order.class).create();
    existing = orderRepository.save(existing);

    orderService.delete(existing);

    Optional<Order> result = orderRepository.findById(existing.getId());
    assertFalse(result.isPresent());
  }

  @Test
  void deleteById_existingOrder_removesOrder() {
    Order existing = Instancio.of(Order.class).create();
    existing = orderRepository.save(existing);

    orderService.deleteById(existing.getId());

    Optional<Order> result = orderRepository.findById(existing.getId());
    assertFalse(result.isPresent());
  }

  @Test
  void saveAll_multipleNewOrders_createsAllOrdersWithIds() {
    var orders = Instancio.ofList(Order.class).size(3).create();

    Iterable<Order> saved = orderService.saveAll(orders);

    assertNotNull(saved);
    saved.forEach(order -> assertNotNull(order.getId(), "Saved order must have an ID"));

    long countInDb = orderRepository.count();
    assertTrue(countInDb >= 3, "Repository should contain at least 3 orders");
  }

  @Test
  void saveAll_existingOrders_updatesAllOrders() {
    var existingOrders = Instancio.ofList(Order.class).size(2).create();
    existingOrders = orderRepository.saveAll(existingOrders);

    existingOrders.forEach(o -> o.setDescription("Updated Description"));

    Iterable<Order> updated = orderService.saveAll(existingOrders);

    updated.forEach(order -> assertEquals("Updated Description", order.getDescription()));

    updated.forEach(
        o -> {
          Optional<Order> fromDb = orderRepository.findById(o.getId());
          assertTrue(fromDb.isPresent());
          assertEquals("Updated Description", fromDb.get().getDescription());
        });
  }

  @Test
  void saveAll_emptyList_returnsEmptyIterable() {
    Iterable<Order> saved = orderService.saveAll(List.of());

    assertNotNull(saved);
    assertFalse(saved.iterator().hasNext(), "Saving empty list should return empty iterable");
  }
}
