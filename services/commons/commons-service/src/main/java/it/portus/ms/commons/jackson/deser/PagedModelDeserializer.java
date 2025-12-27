package it.portus.ms.commons.jackson.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.portus.ms.commons.jackson.deser.utils.DeserializerUtils;
import java.io.IOException;
import java.util.*;
import org.springframework.hateoas.*;

public class PagedModelDeserializer<T> extends StdDeserializer<PagedModel<T>> {

  private final JavaType contentType;

  public PagedModelDeserializer(JavaType contentType) {
    super(PagedModel.class);
    this.contentType = contentType;
  }

  @Override
  public PagedModel<T> deserialize(JsonParser parser, DeserializationContext ctxt)
      throws IOException {

    ObjectNode node = parser.readValueAsTree();

    List<T> content = extractContent(node, ctxt);
    PagedModel.PageMetadata metadata = extractPageMetadata(node, ctxt);
    Links links = DeserializerUtils.extractHateoasLinks(node);

    return PagedModel.of(content, metadata, links);
  }

  private List<T> extractContent(ObjectNode node, DeserializationContext ctxt) throws IOException {
    if (!node.has(JsonFields.EMBEDDED)) {
      return List.of();
    }

    ObjectNode embeddedNode = (ObjectNode) node.get(JsonFields.EMBEDDED);

    if (embeddedNode.isEmpty()) {
      return List.of();
    }

    Optional<Map.Entry<String, JsonNode>> firstField =
        embeddedNode.properties().stream().findFirst();

    if (firstField.isEmpty()) {
      return List.of();
    } else {
      var arrayNode = firstField.get().getValue();

      return ctxt.readTreeAsValue(
          arrayNode, ctxt.getTypeFactory().constructCollectionType(List.class, contentType));
    }
  }

  private PagedModel.PageMetadata extractPageMetadata(ObjectNode node, DeserializationContext ctxt)
      throws IOException {
    if (!node.has(JsonFields.PAGE)) {
      return PagedModel.NO_PAGE.getMetadata();
    }
    return ctxt.readTreeAsValue(node.get(JsonFields.PAGE), PagedModel.PageMetadata.class);
  }

  private static class JsonFields {
    static final String EMBEDDED = "_embedded";
    static final String PAGE = "page";
  }
}
