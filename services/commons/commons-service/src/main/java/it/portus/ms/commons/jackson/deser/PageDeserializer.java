package it.portus.ms.commons.jackson.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class PageDeserializer<T> extends StdDeserializer<Page<T>> {

  private final JavaType contentType;

  public PageDeserializer(JavaType contentType) {
    super(Page.class);
    this.contentType = contentType;
  }

  @Override
  public Page<T> deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
    ObjectNode node = parser.readValueAsTree();

    List<T> content = extractContent(node, ctxt);
    Pageable pageable = extractPageable(node, ctxt);
    long totalElements = extractTotalElements(node, content);

    return new PageImpl<>(content, pageable, totalElements);
  }

  private List<T> extractContent(ObjectNode node, DeserializationContext ctxt) throws IOException {
    if (!node.has(JsonFields.CONTENT) || !node.get(JsonFields.CONTENT).isArray()) {
      return List.of();
    }
    return ctxt.readTreeAsValue(
        node.get(JsonFields.CONTENT),
        ctxt.getTypeFactory().constructCollectionType(List.class, contentType));
  }

  private Pageable extractPageable(ObjectNode node, DeserializationContext ctxt)
      throws IOException {
    if (!node.has(JsonFields.PAGEABLE) || node.get(JsonFields.PAGEABLE).isNull()) {
      return Pageable.unpaged();
    }
    return ctxt.readTreeAsValue(node.get(JsonFields.PAGEABLE), Pageable.class);
  }

  private long extractTotalElements(ObjectNode node, List<T> content) {
    return node.has(JsonFields.TOTAL_ELEMENTS)
        ? node.get(JsonFields.TOTAL_ELEMENTS).asLong()
        : content.size();
  }

  private static class JsonFields {
    static final String CONTENT = "content";
    static final String PAGEABLE = "pageable";
    static final String TOTAL_ELEMENTS = "totalElements";
  }
}
