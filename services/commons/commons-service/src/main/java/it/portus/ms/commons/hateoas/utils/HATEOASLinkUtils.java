package it.portus.ms.commons.hateoas.utils;

import it.portus.ms.commons.http.utils.HttpRequestUtils;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;

@UtilityClass
@Slf4j
public class HATEOASLinkUtils {

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}:]+)(:([^}]*))?}");

  public static List<Link> buildLinks(Class<?> controllerClazz, Environment env, Link... links) {
    return Arrays.stream(links).map(l -> buildLink(controllerClazz, env, l)).toList();
  }

  public static Link buildLink(Class<?> controllerClazz, Environment env, Link rawLink) {
    return applyPlaceholder(controllerClazz, env, rawLink);
  }

  @SuppressWarnings("java:S3011") // Required due to missing public API in Spring HATEOAS
  @SneakyThrows
  public Affordance buildAffordance(
      Class<?> controllerClazz, Environment env, Affordance affordance) {
    if (extractPlaceholder(controllerClazz).isEmpty()) {
      return affordance;
    }

    Field linkField = AffordanceModel.class.getDeclaredField("link");
    linkField.setAccessible(true);

    for (AffordanceModel model : getAffordanceModels(affordance).values()) {
      linkField.set(model, buildLink(controllerClazz, env, model.getLink()));
    }

    return affordance;
  }

  private static Link applyPlaceholder(Class<?> controllerClazz, Environment env, Link rawLink) {
    Optional<Placeholder> placeholderOpt = extractPlaceholder(controllerClazz);
    if (placeholderOpt.isEmpty()) {
      return rawLink;
    }

    Placeholder ph = placeholderOpt.get();
    log.trace("Extracted placeholder from controller {}: {}", controllerClazz.getSimpleName(), ph);

    return getNewLink(
        rawLink,
        HRefUtils.getReplacedHref(
                rawLink.getHref(),
                ph.key + ph.defaultValue,
                HttpRequestUtils.getForwardedPrefixIfFiltered()
                    .orElseGet(() -> getResolvedBasePath(ph, env)))
            .toUriString());
  }

  private String getResolvedBasePath(Placeholder ph, Environment env) {
    String envValue = env.getProperty(ph.key);
    log.trace("Environment variable {}: {}", ph.key, envValue);

    String resolvedBasePath = Objects.requireNonNullElse(envValue, ph.defaultValue);
    log.trace("Resolved base path from placeholder: {}", resolvedBasePath);

    return resolvedBasePath;
  }

  private static Optional<Placeholder> extractPlaceholder(Class<?> controllerClass) {
    RequestMapping mapping = controllerClass.getAnnotation(RequestMapping.class);
    if (mapping == null || mapping.value().length == 0) {
      return Optional.empty();
    }

    return Arrays.stream(mapping.value())
        .filter(StringUtils::isNotBlank)
        .map(
            path -> {
              Matcher matcher = PLACEHOLDER_PATTERN.matcher(path);
              return matcher.find() ? new Placeholder(matcher.group(1), matcher.group(3)) : null;
            })
        .filter(Objects::nonNull)
        .findFirst();
  }

  @SneakyThrows
  private Link getNewLink(Link link, String href) {
    UriTemplate newTemplate = UriTemplate.of(href, new TemplateVariables(link.getVariables()));

    return Link.of(newTemplate, link.getRel())
        .withName(link.getName())
        .withTitle(link.getTitle())
        .withMedia(link.getMedia())
        .withType(link.getType())
        .withProfile(link.getProfile())
        .withHreflang(link.getHreflang())
        .withDeprecation(link.getDeprecation())
        .withAffordances(link.getAffordances());
  }

  @SuppressWarnings({
    "unchecked",
    "java:S3011" // Required due to missing public API in Spring HATEOAS
  })
  @SneakyThrows
  private static Map<MediaType, AffordanceModel> getAffordanceModels(Affordance aff) {
    Field modelsField = Affordance.class.getDeclaredField("models");
    modelsField.setAccessible(true);
    return (Map<MediaType, AffordanceModel>) modelsField.get(aff);
  }

  private record Placeholder(String key, String defaultValue) {}
}
