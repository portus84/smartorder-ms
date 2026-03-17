package it.portus.smartorder.ms.orderservice.business.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.portus.smartorder.ms.orderservice.business.conf.CacheConfiguration;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.domain.model.OrderStatus;
import it.portus.smartorder.ms.orderservice.business.domain.repositories.OrderRepository;
import it.portus.smartorder.ms.orderservice.business.rules.order.OrderRuleEngine;
import it.portus.smartorder.ms.orderservice.business.services.impl.OrderServiceImpl;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@DataMongoTest
@EnableCaching
@Import({OrderServiceImpl.class, CacheConfiguration.class})
@ImportAutoConfiguration
class OrderServiceCacheTest {

  @Autowired private OrderService orderService;

  @Autowired private CacheManager cacheManager;

  @MockitoSpyBean private OrderRepository orderRepository;

  @MockitoBean private OrderRuleEngine rulesService;

  @BeforeEach
  void setupMocks() {
    when(rulesService.applyRules(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    when(rulesService.applyStatusTransition(any(Order.class), any(OrderStatus.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void findById_whenCalledTwice_shouldHitRepositoryOnlyOnce() {
    Order order = Instancio.of(Order.class).create();
    order = orderRepository.save(order);

    orderService.findById(order.getId());

    orderService.findById(order.getId());

    verify(orderRepository, times(1)).findById(order.getId());
  }

  @Test
  void findAll_whenCalledTwice_shouldHitRepositoryOnlyOnce() {
    Pageable pageable = Pageable.ofSize(10);

    orderService.findAll(pageable);
    orderService.findAll(pageable);

    verify(orderRepository, times(1)).findAll(pageable);
  }

  @Test
  void save_shouldPutOrderInCache() {
    Order order = Instancio.of(Order.class).create();

    Order saved = orderService.save(order);

    Cache cache = cacheManager.getCache("orders");
    assertNotNull(cache);

    Order cached = cache.get(saved.getId().toString(), Order.class);

    assertNotNull(cached);
    assertEquals(saved.getId(), cached.getId());
  }

  @Test
  void update_shouldUpdateCacheAndEvictList() {
    Order order = Instancio.of(Order.class).create();
    order = orderRepository.save(order);

    order.setDescription("Updated Description");
    orderService.update(order.getId(), order);

    Cache cache = cacheManager.getCache("orders");
    assertNotNull(cache);

    Order cached = cache.get(order.getId().toString(), Order.class);
    assertNotNull(cached);
    assertEquals("Updated Description", cached.getDescription());

    Cache listCache = cacheManager.getCache("orders_all");
    assertNotNull(listCache);
    assertNull(listCache.get("unpaged"));
  }

  @Test
  void deleteById_shouldEvictCache() {
    Order order = Instancio.of(Order.class).create();
    order = orderRepository.save(order);

    orderService.findById(order.getId());
    Cache cache = cacheManager.getCache("orders");
    assertNotNull(cache);

    assertNotNull(cache.get(order.getId().toString()));

    orderService.deleteById(order.getId());

    assertNull(cache.get(order.getId().toString()));
  }

  @Test
  void delete_shouldEvictCache() {
    Order order = Instancio.of(Order.class).create();
    order = orderRepository.save(order);

    orderService.findById(order.getId());

    Cache cache = cacheManager.getCache("orders");
    assertNotNull(cache);

    assertNotNull(cache.get(order.getId().toString()));

    orderService.delete(order);

    assertNull(cache.get(order.getId().toString()));
  }
}
