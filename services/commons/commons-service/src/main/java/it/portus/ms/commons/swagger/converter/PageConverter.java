package it.portus.ms.commons.swagger.converter;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageConverter implements ModelConverter {

  private static final class Properties {
    static final String PAGEABLE = "pageable";
    static final String SORT = "sort";
    static final String[] TO_REMOVE = {PAGEABLE, SORT};
  }

  @Override
  public Schema<?> resolve(
      AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
    if (type.getType() != null && type.getType().equals(Page.class)) {

      Schema<?> schema = chain.hasNext() ? chain.next().resolve(type, context, chain) : null;

      Optional.ofNullable(schema)
          .map(Schema::getProperties)
          .ifPresent(props -> Arrays.stream(Properties.TO_REMOVE).forEach(props::remove));

      return schema;
    }

    return chain.hasNext() ? chain.next().resolve(type, context, chain) : null;
  }
}
