package it.portus.ms.commons.jackson.module;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;

public class AbstractDeserializerModule<T> extends SimpleModule {

  private final Class<? super T> rawType;
  private final transient Function<JavaType, JsonDeserializer<? extends T>> deserializerFactory;

  public AbstractDeserializerModule(
      @NonNull Class<? super T> rawType,
      @NonNull Function<JavaType, JsonDeserializer<? extends T>> factory) {
    this.rawType = rawType;
    this.deserializerFactory = factory;
  }

  @Override
  public void setupModule(SetupContext context) {
    super.setupModule(context);

    context.addDeserializers(
        new Deserializers.Base() {

          @Override
          public JsonDeserializer<?> findBeanDeserializer(
              JavaType javaType, DeserializationConfig config, BeanDescription beanDesc) {
            if (rawType.equals(javaType.getRawClass())) {
              return deserializerFactory.apply(javaType);
            }

            return null;
          }

          @Override
          public boolean hasDeserializerFor(DeserializationConfig config, Class<?> valueType) {
            return rawType.isAssignableFrom(valueType);
          }
        });
  }
}
