package it.portus.ms.commons.jackson.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.StreamSupport;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageableDeserializer extends JsonDeserializer<Pageable> {

  @Override
  public Pageable deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
    ObjectNode node = parser.readValueAsTree();

    OptionalInt pageNumber = getOptionalInt(node, JsonFields.PAGE_NUMBER);
    OptionalInt pageSize = getOptionalInt(node, JsonFields.PAGE_SIZE);
    Optional<Boolean> paged = getOptionalBoolean(node);

    Sort sort = extractSort(node);

    if (!paged.orElse(true)) {
      return Pageable.unpaged();
    }

    if (pageNumber.isPresent() && pageSize.isPresent()) {
      return PageRequest.of(pageNumber.getAsInt(), pageSize.getAsInt(), sort);
    } else if (pageSize.isPresent()) {
      return PageRequest.ofSize(pageSize.getAsInt());
    } else {
      return Pageable.unpaged();
    }
  }

  private OptionalInt getOptionalInt(ObjectNode node, String field) {
    if (node.has(field) && node.get(field).isInt()) {
      return OptionalInt.of(node.get(field).asInt());
    }
    return OptionalInt.empty();
  }

  private Optional<Boolean> getOptionalBoolean(ObjectNode node) {
    if (node.has(JsonFields.PAGED) && node.get(JsonFields.PAGED).isBoolean()) {
      return Optional.of(node.get(JsonFields.PAGED).asBoolean());
    }
    return Optional.empty();
  }

  private Sort extractSort(ObjectNode node) {
    if (!node.has(JsonFields.SORT) || !node.get(JsonFields.SORT).isObject()) {
      return Sort.unsorted();
    }

    ObjectNode sortNode = (ObjectNode) node.get(JsonFields.SORT);
    ArrayNode ordersArray = (ArrayNode) sortNode.get(JsonFields.ORDERS);

    if (ordersArray == null || ordersArray.isEmpty()) {
      return Sort.unsorted();
    }

    List<Sort.Order> orders =
        StreamSupport.stream(ordersArray.spliterator(), false)
            .filter(ObjectNode.class::isInstance)
            .map(ObjectNode.class::cast)
            .map(this::mapToSortOrder)
            .toList();

    return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
  }

  private Sort.Order mapToSortOrder(ObjectNode orderNode) {
    String property = orderNode.path(JsonFields.PROPERTY).asText();
    String direction = orderNode.path(JsonFields.DIRECTION).asText(Sort.Direction.ASC.name());
    boolean ascending = !direction.equalsIgnoreCase(Sort.Direction.DESC.name());
    return ascending ? Sort.Order.asc(property) : Sort.Order.desc(property);
  }

  private static class JsonFields {
    static final String PAGE_NUMBER = "pageNumber";
    static final String PAGE_SIZE = "pageSize";
    static final String PAGED = "paged";
    static final String SORT = "sort";
    static final String ORDERS = "orders";
    static final String PROPERTY = "property";
    static final String DIRECTION = "direction";
  }
}
