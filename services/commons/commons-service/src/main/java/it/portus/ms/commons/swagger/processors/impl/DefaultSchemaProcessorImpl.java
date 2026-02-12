package it.portus.ms.commons.swagger.processors.impl;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import it.portus.ms.commons.dto.ErrorResponse;
import it.portus.ms.commons.dto.PageMetadata;
import it.portus.ms.commons.swagger.processors.OpenApiSchemaProcessor;
import it.portus.ms.commons.swagger.processors.utils.OpenApiSchemaUtils;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass(org.springframework.data.domain.Page.class)
public class DefaultSchemaProcessorImpl implements OpenApiSchemaProcessor {

  private final Set<String> utilitySchemas =
      new LinkedHashSet<>(
          Set.of(
              ErrorResponse.class.getSimpleName(),
              Pageable.class.getSimpleName(),
              Page.class.getSimpleName(),
              PageMetadata.class.getSimpleName()));

  private final Set<String> internalSchemas = new LinkedHashSet<>();

  private final Map<String, Schema<?>> replaceSchemas = new LinkedHashMap<>();

  public DefaultSchemaProcessorImpl() {
    resolveSchemaFromClass(PageMetadata.class)
        .ifPresent(
            schema ->
                addReplaceSchemas(
                    Map.ofEntries(Map.entry(PageMetadata.class.getSimpleName(), schema))));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void processSchemas(OpenAPI openApi, Map<String, Schema<?>> originalSchemas) {
    originalSchemas.replaceAll(replaceSchemas::getOrDefault);

    Map<String, Schema<?>> newOrderedSchemas = reorderSchemas(originalSchemas);

    Components components = Optional.ofNullable(openApi.getComponents()).orElseGet(Components::new);
    components.setSchemas((Map<String, Schema>) (Map<?, ?>) newOrderedSchemas);

    Map<String, Schema<?>> lookup = collectAllInternalSchemas(newOrderedSchemas);
    inlineAllInternalSchemas(newOrderedSchemas, lookup);

    removeInternalSchemas(newOrderedSchemas);
    openApi.setComponents(components);
  }

  @Override
  public void addUtilitySchemas(Set<String> schemaNames) {
    utilitySchemas.addAll(schemaNames);
  }

  @Override
  public void addInternalSchemas(Set<String> schemaNames) {
    internalSchemas.addAll(schemaNames);
  }

  @Override
  public void addReplaceSchemas(Map<String, Schema<?>> schemaNames) {
    replaceSchemas.putAll(schemaNames);
  }

  private Map<String, Schema<?>> reorderSchemas(Map<String, Schema<?>> originalSchemas) {
    SortedMap<String, Schema<?>> filteredMainSchemas =
        OpenApiSchemaUtils.filterSchemas(
            originalSchemas, e -> !utilitySchemas.contains(e.getKey()));

    SortedMap<String, Schema<?>> filteredUtilitySchemas =
        OpenApiSchemaUtils.filterSchemas(
            originalSchemas, e -> this.utilitySchemas.contains(e.getKey()));

    LinkedHashMap<String, Schema<?>> ordered = new LinkedHashMap<>();
    ordered.putAll(filteredMainSchemas);
    ordered.putAll(filteredUtilitySchemas);

    return ordered;
  }

  private Map<String, Schema<?>> collectAllInternalSchemas(Map<String, Schema<?>> schemas) {
    Map<String, Schema<?>> lookup = new HashMap<>();
    schemas.values().forEach(schema -> collectSchemas(schema, lookup, schemas));
    return lookup;
  }

  private void inlineAllInternalSchemas(
      Map<String, Schema<?>> schemas, Map<String, Schema<?>> lookup) {
    schemas.values().forEach(schema -> OpenApiSchemaUtils.inlineInternalSchemas(schema, lookup));
  }

  private void removeInternalSchemas(Map<String, Schema<?>> schemas) {
    schemas.keySet().removeIf(this::matchInternalSchemas);
  }

  private void collectSchemas(
      Schema<?> schema, Map<String, Schema<?>> lookup, Map<String, Schema<?>> allSchemas) {
    Optional.ofNullable(schema)
        .ifPresent(
            s -> {
              Optional.ofNullable(s.get$ref())
                  .ifPresent(
                      ref -> {
                        String refName = StringUtils.substringAfterLast(ref, "/");
                        if (matchInternalSchemas(refName) && !lookup.containsKey(refName)) {
                          Schema<?> actual =
                              Optional.ofNullable(allSchemas.get(refName))
                                  .or(
                                      () ->
                                          switch (refName) {
                                            case "Link" ->
                                                resolveSchemaFromClass(
                                                    it.portus.ms.commons.dto.Link.class);
                                            case "Links" ->
                                                resolveSchemaFromClass(
                                                    it.portus.ms.commons.dto.Links.class);
                                            default -> Optional.empty();
                                          })
                                  .orElse(null);

                          Optional.ofNullable(actual)
                              .ifPresent(
                                  a -> {
                                    lookup.put(refName, a);
                                    collectSchemas(a, lookup, allSchemas);
                                  });
                        }
                      });

              if (s instanceof ArraySchema arraySchema) {
                collectSchemas(arraySchema.getItems(), lookup, allSchemas);
              }

              Optional.ofNullable(s.getProperties())
                  .ifPresent(
                      props ->
                          props.values().forEach(prop -> collectSchemas(prop, lookup, allSchemas)));

              Optional.ofNullable(s.getAdditionalProperties())
                  .filter(Schema.class::isInstance)
                  .map(Schema.class::cast)
                  .ifPresent(addProps -> collectSchemas(addProps, lookup, allSchemas));
            });
  }

  private boolean matchInternalSchemas(String schemaName) {
    return internalSchemas.stream()
        .anyMatch(
            sn -> Strings.CS.startsWith(schemaName, sn) || Strings.CS.endsWith(schemaName, sn));
  }

  protected <T> Optional<Schema<T>> resolveSchemaFromClass(Class<?> clazz) {
    return OpenApiSchemaUtils.resolveSchemaFromClass(clazz);
  }
}
