package it.portus.ms.commons.jackson.module;

import it.portus.ms.commons.jackson.deser.PageDeserializer;
import org.springframework.data.domain.Page;

public class PageModule extends AbstractDeserializerModule<Page<?>> {
  public PageModule() {
    super(Page.class, javaType -> new PageDeserializer<>(javaType.containedTypeOrUnknown(0)));
  }
}
