package it.portus.ms.commons.logging.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import lombok.Getter;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.context.annotation.Configuration;

class LoggingAspectUtilsTest {

  @Test
  void isBusinessClass_ValidBusinessClass_ReturnsTrue() {
    try (DynamicType.Unloaded<Object> type =
        new ByteBuddy().subclass(Object.class).name("com.company.test.MyBusinessClass").make()) {
      Class<?> dynamicClass =
          type.load(getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
              .getLoaded();

      assertTrue(LoggingAspectUtils.isBusinessClass(dynamicClass));
    }
  }

  @Test
  void getTargetClass_TargetIsNull_ReturnsEmptyOptional() {
    ProceedingJoinPoint joinPoint = new MockJoinPoint(null, null);
    Optional<Class<?>> result = LoggingAspectUtils.getTargetClass(joinPoint);
    assertTrue(result.isEmpty());
  }

  @Test
  void getTargetClass_TargetHasClass_ReturnsClass() {
    MyService service = new MyService();
    ProceedingJoinPoint joinPoint = new MockJoinPoint(service, null);
    Optional<Class<?>> result = LoggingAspectUtils.getTargetClass(joinPoint);
    assertTrue(result.isPresent());
    assertEquals(MyService.class, result.get());
  }

  @Test
  void getLoggerClass_ClassWithInterface_ReturnsFirstInterface() {
    Class<?> result = LoggingAspectUtils.getLoggerClass(MyService.class);
    assertEquals(MyInterface.class, result);
  }

  @Test
  void getLoggerClass_ClassWithoutInterface_ReturnsClassItself() {
    class NoInterfaces {}
    Class<?> result = LoggingAspectUtils.getLoggerClass(NoInterfaces.class);
    assertEquals(NoInterfaces.class, result);
  }

  @Test
  void isBusinessClass_ExcludedByKeyword_ReturnsFalse() {
    assertFalse(LoggingAspectUtils.isBusinessClass(String.class));
  }

  @Test
  void isBusinessClass_BeanConfigurationClass_ReturnsFalse() {
    assertFalse(LoggingAspectUtils.isBusinessClass(ConfigClass.class));
  }

  @Test
  void isBusinessClass_AopInfrastructureBean_ReturnsFalse() {
    assertFalse(LoggingAspectUtils.isBusinessClass(MyAopBean.class));
  }

  @Test
  void formatArgs_ArgsIsNull_ReturnsEmptyBrackets() {
    String result = LoggingAspectUtils.formatArgs(null);
    assertEquals("[]", result);
  }

  @Test
  void formatArgs_ArgsIsEmpty_ReturnsEmptyBrackets() {
    String result = LoggingAspectUtils.formatArgs(new Object[0]);
    assertEquals("[]", result);
  }

  @Test
  void formatArgs_ArgsWithValues_ReturnsFormattedString() {
    String result = LoggingAspectUtils.formatArgs(new Object[] {1, null, "abc"});
    assertEquals("[1, null, abc]", result);
  }

  interface MyInterface {}

  static class MyService implements MyInterface {}

  @Configuration
  static class ConfigClass {}

  static class MyAopBean implements AopInfrastructureBean {}

  static class MockJoinPoint implements ProceedingJoinPoint {

    @Getter private final Object target;
    @Getter private final Signature signature;

    MockJoinPoint(Object target, Signature signature) {
      this.target = target;
      this.signature = signature;
    }

    @Override
    public Object proceed() {
      return null;
    }

    @Override
    public Object proceed(Object[] args) {
      return null;
    }

    @Override
    public void set$AroundClosure(AroundClosure arc) {}

    @Override
    public Object getThis() {
      return null;
    }

    @Override
    public Object[] getArgs() {
      return new Object[0];
    }

    @Override
    public SourceLocation getSourceLocation() {
      return null;
    }

    @Override
    public String getKind() {
      return "";
    }

    @Override
    public JoinPoint.StaticPart getStaticPart() {
      return null;
    }

    @Override
    public String toShortString() {
      return "";
    }

    @Override
    public String toLongString() {
      return "";
    }
  }
}
