package it.portus.ms.commons.swagger.processors.utils;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

@UtilityClass
public class OpenApiSchemaUtils {

  @SuppressWarnings("java:S1452") // Wildcard used intentionally due to heterogeneous schema types
  public static SortedMap<String, Schema<?>> filterSchemas(
      Map<String, Schema<?>> schemas, Predicate<Map.Entry<String, Schema<?>>> filter) {
    return schemas.entrySet().stream()
        .filter(filter)
        .collect(
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, TreeMap::new));
  }

  public static void inlineInternalSchemas(Schema<?> schema, Map<String, Schema<?>> lookup) {
    Optional.ofNullable(schema)
        .ifPresent(
            s -> {
              Optional.ofNullable(s.getProperties())
                  .ifPresent(
                      props ->
                          props.replaceAll(
                              (name, propSchema) -> {
                                Schema<?> resolved = resolveRef(propSchema, lookup);
                                inlineInternalSchemas(resolved, lookup);
                                return resolved;
                              }));

              Object additional = s.getAdditionalProperties();
              if (additional instanceof Schema<?> addProps) {
                resolveAndInline(addProps, lookup, s::setAdditionalProperties);
              }

              if (s instanceof ArraySchema arraySchema) {
                Optional.ofNullable(arraySchema.getItems())
                    .ifPresent(item -> resolveAndInline(item, lookup, arraySchema::setItems));
              }

              if (s instanceof MapSchema mapSchema) {
                Optional.ofNullable(mapSchema.getItems())
                    .ifPresent(item -> resolveAndInline(item, lookup, mapSchema::setItems));
              }
            });
  }

  @SuppressWarnings({"unchecked"})
  public static <T> Optional<Schema<T>> resolveSchemaFromClass(@Nullable Class<?> clazz) {
    if (clazz == null) {
      return Optional.empty();
    }

    ResolvedSchema resolved =
        ModelConverters.getInstance().readAllAsResolvedSchema(new AnnotatedType(clazz));

    return Optional.ofNullable(resolved.schema)
        .or(() -> resolved.referencedSchemas.values().stream().findFirst())
        .map(Schema.class::cast);
  }

  private static void resolveAndInline(
      Schema<?> target, Map<String, Schema<?>> lookup, Consumer<Schema<?>> setter) {
    if (target != null) {
      Schema<?> resolved = resolveRef(target, lookup);
      setter.accept(resolved);
      inlineInternalSchemas(resolved, lookup);
    }
  }

  private static Schema<?> resolveRef(Schema<?> propSchema, Map<String, Schema<?>> lookup) {
    return Optional.ofNullable(propSchema)
        .filter(s -> StringUtils.isNotBlank(s.get$ref()))
        .map(
            s -> {
              String refName = StringUtils.substringAfterLast(s.get$ref(), "/");
              return Optional.ofNullable(lookup.get(refName))
                  .map(OpenApiSchemaUtils::cloneSchema)
                  .map(Schema.class::cast)
                  .orElse(s);
            })
        .orElse(propSchema);
  }

  private static Schema<?> cloneSchema(@Nullable Schema<?> original) {
    if (original == null) return null;

    Schema<?> clone;
    try {
      clone = original.getClass().getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to clone Schema: " + original.getClass(), e);
    }

    if (StringUtils.isNotBlank(original.get$ref())) {
      clone.set$ref(original.get$ref());
      return clone;
    }

    Optional.ofNullable(original.getType()).ifPresent(clone::setType);
    Optional.ofNullable(original.getFormat()).ifPresent(clone::setFormat);
    Optional.ofNullable(original.getDescription()).ifPresent(clone::setDescription);
    Optional.ofNullable(original.getExample()).ifPresent(clone::setExample);
    Optional.ofNullable(original.getNullable()).ifPresent(clone::setNullable);
    Optional.ofNullable(original.getReadOnly()).ifPresent(clone::setReadOnly);
    Optional.ofNullable(original.getWriteOnly()).ifPresent(clone::setWriteOnly);
    Optional.ofNullable(original.getDeprecated()).ifPresent(clone::setDeprecated);
    Optional.ofNullable(original.getRequired())
        .ifPresent(req -> clone.setRequired(new ArrayList<>(req)));

    Optional.ofNullable(original.getItems()).ifPresent(item -> clone.setItems(cloneSchema(item)));

    Optional.ofNullable(original.getProperties())
        .ifPresent(
            props -> {
              @SuppressWarnings({"rawtypes"})
              Map<String, Schema> clonedProps = new LinkedHashMap<>();
              props.forEach((k, v) -> clonedProps.put(k, cloneSchema(v)));
              if (!clonedProps.isEmpty()) clone.setProperties(clonedProps);
            });

    Object ap = original.getAdditionalProperties();
    if (ap instanceof Schema<?> apSchema) {
      clone.setAdditionalProperties(cloneSchema(apSchema));
    } else if (ap instanceof Boolean apBool) {
      clone.setAdditionalProperties(apBool);
    }

    Optional.ofNullable(original.getExtensions())
        .ifPresent(
            ext -> {
              if (!ext.isEmpty()) clone.setExtensions(new LinkedHashMap<>(ext));
            });

    return clone;
  }
}
