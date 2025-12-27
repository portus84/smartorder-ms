package it.portus.ms.commons.swagger.processors.impl;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Primary;
import org.springframework.data.web.config.HateoasAwareSpringDataWebConfiguration;
import org.springframework.hateoas.*;
import org.springframework.stereotype.Component;

@Primary
@Component
@ConditionalOnClass(HateoasAwareSpringDataWebConfiguration.class)
public class HateoasProcessorImpl extends DefaultSchemaProcessorImpl {

  public HateoasProcessorImpl() {
    addUtilitySchemas(
        Set.of(
            EntityModel.class.getSimpleName(),
            PagedModel.class.getSimpleName(),
            CollectionModel.class.getSimpleName(),
            Links.class.getSimpleName(),
            Link.class.getSimpleName()));

    addInternalSchemas(
        Set.of(
            "Embedded",
            PagedModel.class.getSimpleName(),
            RepresentationModel.class.getSimpleName(),
            Links.class.getSimpleName(),
            Link.class.getSimpleName()));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  protected <T> Optional<Schema<T>> resolveSchemaFromClass(Class<?> clazz) {
    ResolvedSchema resolved =
        ModelConverters.getInstance().readAllAsResolvedSchema(new AnnotatedType(clazz));

    return Optional.ofNullable(resolved.schema)
        .or(
            () -> {
              if (Links.class.equals(clazz)) {
                Schema linkSchema = resolved.referencedSchemas.get("Link");
                ObjectSchema linksSchema = new ObjectSchema();
                linksSchema.additionalProperties(linkSchema);
                linksSchema.setDescription("HATEOAS Links");
                return Optional.of(linksSchema);
              }

              return super.resolveSchemaFromClass(clazz);
            })
        .map(Schema.class::cast);
  }
}
