package it.portus.ms.commons.jackson.module;

import it.portus.ms.commons.jackson.deser.EntityModelDeserializer;
import org.springframework.hateoas.EntityModel;

public class EntityModelModule extends AbstractDeserializerModule<EntityModel<?>> {
  public EntityModelModule() {
    super(
        EntityModel.class,
        javaType -> new EntityModelDeserializer<>(javaType.containedTypeOrUnknown(0)));
  }
}
