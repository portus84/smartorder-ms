package it.portus.ms.commons.jackson.deser.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;

class DeserializerUtilsTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String BASE_URL = "https://example.org";
  private static final String SELF_CONTEXT_PATH = "/" + IanaLinkRelations.SELF.value();

  @Test
  void extractHateoasLinks_NullNode_ReturnsLinksNone() {
    Links result = DeserializerUtils.extractHateoasLinks(null);
    assertEquals(Links.NONE, result);
  }

  @Test
  void extractHateoasLinks_NodeWithoutSpecifiedProperty_ReturnsLinksNone() {
    ObjectNode node = MAPPER.createObjectNode();
    node.put("foo", "bar");

    Links result = DeserializerUtils.extractHateoasLinks(node);

    assertEquals(Links.NONE, result);
  }

  @Test
  void extractHateoasLinks_ValidSingleLinkNode_ReturnsOneLink() {
    ObjectNode hrefNode = MAPPER.createObjectNode().put("href", BASE_URL + SELF_CONTEXT_PATH);

    ObjectNode linksNode = MAPPER.createObjectNode();
    linksNode.set(IanaLinkRelations.SELF.value(), hrefNode);

    ObjectNode rootNode = MAPPER.createObjectNode();
    rootNode.set("_links", linksNode);

    Links result = DeserializerUtils.extractHateoasLinks(rootNode);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.toList().size());

    Link link = result.toList().getFirst();
    assertEquals(IanaLinkRelations.SELF.value(), link.getRel().value());
    assertEquals(BASE_URL + SELF_CONTEXT_PATH, link.getHref());
  }

  @Test
  void extractHateoasLinks_MultipleLinks_ReturnsMultipleLinks() {
    String randomSegment = Instancio.create(String.class);

    ObjectNode selfNode =
        MAPPER.createObjectNode().put("href", BASE_URL + "/" + randomSegment + SELF_CONTEXT_PATH);
    ObjectNode nextNode =
        MAPPER
            .createObjectNode()
            .put("href", BASE_URL + "/" + randomSegment + "/" + IanaLinkRelations.NEXT.value());

    ObjectNode linksNode = MAPPER.createObjectNode();
    linksNode.set(IanaLinkRelations.SELF.value(), selfNode);
    linksNode.set(IanaLinkRelations.NEXT.value(), nextNode);

    ObjectNode rootNode = MAPPER.createObjectNode();
    rootNode.set("_links", linksNode);

    Links result = DeserializerUtils.extractHateoasLinks(rootNode);

    assertEquals(2, result.toList().size());
    assertTrue(result.hasLink(IanaLinkRelations.SELF));
    assertTrue(result.hasLink(IanaLinkRelations.NEXT));
  }

  @Test
  void extractHateoasLinks_LinkWithoutHref_ReturnsEmptyLinks() {
    ObjectNode invalidLinkNode = MAPPER.createObjectNode();
    ObjectNode linksNode = MAPPER.createObjectNode();
    linksNode.set(IanaLinkRelations.SELF.value(), invalidLinkNode);

    ObjectNode rootNode = MAPPER.createObjectNode();
    rootNode.set("_links", linksNode);

    Links result = DeserializerUtils.extractHateoasLinks(rootNode);

    assertTrue(result.isEmpty());
  }
}
