package it.portus.ms.commons.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageableUtilsTest {

  @Test
  void of_NullInputs_ReturnsDefaultPageable() {
    Pageable pageable = PageableUtils.of(null, null, null);

    assertEquals(PageableUtils.DEFAULT_PAGE, pageable.getPageNumber());
    assertEquals(PageableUtils.DEFAULT_SIZE, pageable.getPageSize());
    assertFalse(pageable.getSort().isSorted());
  }

  @Test
  void of_EmptySortList_ReturnsUnsortedPageable() {
    Pageable pageable = PageableUtils.of(2, 50, List.of());

    assertEquals(2, pageable.getPageNumber());
    assertEquals(50, pageable.getPageSize());
    assertFalse(pageable.getSort().isSorted());
  }

  @Test
  void of_SingleSortAsc_ReturnsPageableWithAscendingSort() {
    String property = "name";
    Pageable pageable = PageableUtils.of(1, 10, List.of(property + ",asc"));

    Sort.Order order = pageable.getSort().getOrderFor(property);
    assertNotNull(order);
    assertEquals(Sort.Direction.ASC, order.getDirection());
  }

  @Test
  void of_SingleSortDesc_ReturnsPageableWithDescendingSort() {
    String property = "createdAt";
    Pageable pageable = PageableUtils.of(0, 10, List.of(property + ",desc"));

    Sort.Order order = pageable.getSort().getOrderFor(property);
    assertNotNull(order);
    assertEquals(Sort.Direction.DESC, order.getDirection());
  }

  @Test
  void of_MultipleSorts_ReturnsPageableWithMultipleOrders() {
    Pageable pageable = PageableUtils.of(0, 10, List.of("name,asc", "createdAt,desc"));
    List<Sort.Order> orders = pageable.getSort().toList();

    assertEquals(2, orders.size());
    assertEquals("name", orders.get(0).getProperty());
    assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
    assertEquals("createdAt", orders.get(1).getProperty());
    assertEquals(Sort.Direction.DESC, orders.get(1).getDirection());
  }

  @Test
  void of_SortWithoutDirection_DefaultsToAscending() {
    String property = "username";
    Pageable pageable = PageableUtils.of(0, 10, List.of(property));

    Sort.Order order = pageable.getSort().getOrderFor(property);
    assertNotNull(order);
    assertEquals(Sort.Direction.ASC, order.getDirection());
  }

  @Test
  void of_SortWithSpaces_ParsesCorrectly() {
    String property = "email";
    Pageable pageable = PageableUtils.of(0, 10, List.of("  " + property + " ,  desc  "));

    Sort.Order order = pageable.getSort().getOrderFor(property);
    assertNotNull(order);
    assertEquals(Sort.Direction.DESC, order.getDirection());
  }
}
