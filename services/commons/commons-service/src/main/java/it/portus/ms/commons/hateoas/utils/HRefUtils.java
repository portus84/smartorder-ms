package it.portus.ms.commons.hateoas.utils;

import lombok.experimental.UtilityClass;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@UtilityClass
class HRefUtils {

  private static final String PLACEHOLDER_PREFIX = "\\$\\{";
  private static final String PLACEHOLDER_SUFFIX = "(?::[^}]*)?}";

  public static UriComponents getReplacedHref(String href, String part, String replacement) {
    String regex = PLACEHOLDER_PREFIX + part + PLACEHOLDER_SUFFIX;

    String replaced = href.replaceAll(regex, replacement);

    return UriComponentsBuilder.fromUriString(replaced).build();
  }
}
