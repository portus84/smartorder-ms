package it.portus.ms.commons.hateoas.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponents;

class HRefUtilsTest {

  @Test
  void getReplacedHref_PlaceholderExists_ReplacesCorrectly() {
    String href = "http://localhost/${service.base-path/api/v1}/resource/123";
    String expected = "http://localhost/api/v1/resource/123";

    UriComponents result = HRefUtils.getReplacedHref(href, "service.base-path/api/v1", "/api/v1");

    assertEquals(expected, result.toUriString());
  }

  @Test
  void getReplacedHref_PlaceholderWithDefault_ReplacesIgnoringDefault() {
    String href = "http://localhost/api/${id:default}";
    String expected = "http://localhost/api/999";

    UriComponents result = HRefUtils.getReplacedHref(href, "id", "999");

    assertEquals(expected, result.toUriString());
  }

  @Test
  void getReplacedHref_NoPlaceholder_NoChange() {
    String href = "http://localhost/api/resource";

    UriComponents result = HRefUtils.getReplacedHref(href, "id", "xxx");

    assertEquals(href, result.toUriString());
  }

  @Test
  void getReplacedHref_MultiplePlaceholders_AllReplaced() {
    String href = "http://localhost/api/${id}/detail/${id}";
    String expected = "http://localhost/api/42/detail/42";

    UriComponents result = HRefUtils.getReplacedHref(href, "id", "42");

    assertEquals(expected, result.toUriString());
  }

  @Test
  void getReplacedHref_DifferentPlaceholder_NotReplaced() {
    String href = "http://localhost/api/${other}";
    String expected = "http://localhost/api/${other}";

    UriComponents result = HRefUtils.getReplacedHref(href, "id", "xxx");

    assertEquals(expected, result.toUriString());
  }

  @Test
  void getReplacedHref_PlaceholderWithComplexDefault_Replaces() {
    String href = "http://localhost/api/${id:abc-def_123}";
    String expected = "http://localhost/api/custom";

    UriComponents result = HRefUtils.getReplacedHref(href, "id", "custom");

    assertEquals(expected, result.toUriString());
  }
}
