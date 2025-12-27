package it.portus.ms.commons.jackson.deser.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;

@UtilityClass
public class DeserializerUtils {

  static final String LINKS = "_links";

  public static Links extractHateoasLinks(@Nullable ObjectNode node) {
    if (node == null || !node.has(LINKS)) {
      return Links.NONE;
    }

    JsonNode linksNode = node.get(LINKS);
    List<Link> links = new ArrayList<>();

    linksNode
        .properties()
        .forEach(
            link -> {
              String rel = link.getKey();
              JsonNode hrefNode = link.getValue().get("href");
              if (hrefNode != null) {
                links.add(Link.of(hrefNode.asText(), rel));
              }
            });

    return Links.of(links);
  }
}
