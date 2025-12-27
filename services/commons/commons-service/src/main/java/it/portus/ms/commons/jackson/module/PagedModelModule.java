package it.portus.ms.commons.jackson.module;

import it.portus.ms.commons.jackson.deser.PagedModelDeserializer;
import org.springframework.hateoas.PagedModel;

public class PagedModelModule extends AbstractDeserializerModule<PagedModel<?>> {
  public PagedModelModule() {
    super(
        PagedModel.class,
        javaType -> new PagedModelDeserializer<>(javaType.containedTypeOrUnknown(0)));
  }
}
