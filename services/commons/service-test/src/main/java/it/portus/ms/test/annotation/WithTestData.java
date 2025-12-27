package it.portus.ms.test.annotation;

import it.portus.ms.test.data.TestDataLoader;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface WithTestData {
  Class<? extends TestDataLoader>[] value();

  boolean transactional() default true;

  boolean rollback() default true;
}
