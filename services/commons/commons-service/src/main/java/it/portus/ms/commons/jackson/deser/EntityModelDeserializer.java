package it.portus.ms.commons.jackson.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.portus.ms.commons.jackson.deser.utils.DeserializerUtils;
import java.io.IOException;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Links;

public class EntityModelDeserializer<T> extends StdDeserializer<EntityModel<T>> {

  private final JavaType contentType;

  public EntityModelDeserializer(JavaType contentType) {
    super(EntityModel.class);
    this.contentType = contentType;
  }

  @Override
  public EntityModel<T> deserialize(JsonParser parser, DeserializationContext ctxt)
      throws IOException {
    ObjectNode node = parser.readValueAsTree();

    ObjectNode contentNode = node.deepCopy();

    T content = ctxt.readTreeAsValue(contentNode, contentType);

    Links links = DeserializerUtils.extractHateoasLinks(node);

    return EntityModel.of(content, links);
  }
}
