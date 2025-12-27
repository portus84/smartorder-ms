package it.portus.ms.commons.hateoas;

import java.util.Map;
import java.util.stream.Stream;
import org.springframework.hateoas.*;

public class CustomPayloadMetadata implements AffordanceModel.PayloadMetadata {
  private final Class<?> type;
  private final Map<String, Object> template;

  public CustomPayloadMetadata(Class<?> type, Map<String, Object> template) {
    this.type = type;
    this.template = template;
  }

  @Override
  public Stream<AffordanceModel.PropertyMetadata> stream() {
    return Stream.empty();
  }

  @org.springframework.lang.Nullable
  @Override
  public Class<?> getType() {
    return type;
  }

  public Map<String, Object> getTemplate() {
    return template;
  }
}
