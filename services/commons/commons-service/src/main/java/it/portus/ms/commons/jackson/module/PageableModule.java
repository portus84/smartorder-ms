package it.portus.ms.commons.jackson.module;

import com.fasterxml.jackson.databind.module.SimpleModule;
import it.portus.ms.commons.jackson.deser.PageableDeserializer;
import org.springframework.data.domain.Pageable;

public class PageableModule extends SimpleModule {

  public PageableModule() {
    addDeserializer(Pageable.class, new PageableDeserializer());
  }
}
