package it.portus.ms.commons.mappers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.aop.framework.AopProxyUtils;

public abstract class AbstractPageMapper {

  protected final Map<String, PageContentMapper<?, ?>> elementMappers;

  protected AbstractPageMapper(List<PageContentMapper<?, ?>> mappers) {
    this.elementMappers = buildMapperMap(mappers);
  }

  @SuppressWarnings("unchecked")
  protected <S, T> PageContentMapper<S, T> resolveMapper(Class<T> targetClass) {
    String expectedKey = buildMapperKey(targetClass);

    return (PageContentMapper<S, T>)
        elementMappers.entrySet().stream()
            .filter(entry -> entry.getKey().equalsIgnoreCase(expectedKey))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No PageContentMapper found for class: "
                            + targetClass.getName()
                            + " (Expected key: "
                            + expectedKey
                            + ")"));
  }

  private Map<String, PageContentMapper<?, ?>> buildMapperMap(
      List<PageContentMapper<?, ?>> mappers) {
    return mappers.stream()
        .collect(
            Collectors.toMap(
                m -> AopProxyUtils.ultimateTargetClass(m).getSimpleName(), Function.identity()));
  }

  private String buildMapperKey(Class<?> targetClass) {
    return targetClass.getSimpleName() + "MapperImpl";
  }
}
