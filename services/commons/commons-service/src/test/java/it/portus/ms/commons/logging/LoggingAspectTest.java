package it.portus.ms.commons.logging;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.lang.reflect.Method;
import java.util.List;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

  @Service
  static class DummyService {
    public String doWork() {
      return "done";
    }
  }

  private static final String CORRELATION_ID_KEY = "correlationId";

  private LoggingAspect loggingAspect;

  @Mock private ProceedingJoinPoint joinPoint;

  @Mock private MethodSignature methodSignature;

  private ListAppender<ILoggingEvent> listAppender;

  @BeforeEach
  void setUp() throws NoSuchMethodException {
    loggingAspect = new LoggingAspect(List.of("it.portus.ms.commons.logging"));

    Logger targetLogger = (Logger) LoggerFactory.getLogger(DummyService.class);
    targetLogger.setLevel(Level.DEBUG);
    listAppender = new ListAppender<>();
    listAppender.start();
    targetLogger.addAppender(listAppender);

    Method doWorkMethod = DummyService.class.getMethod("doWork");
    lenient().when(joinPoint.getSignature()).thenReturn(methodSignature);
    lenient().when(joinPoint.getTarget()).thenReturn(new DummyService());
    lenient().when(methodSignature.getMethod()).thenReturn(doWorkMethod);
    lenient().when(joinPoint.getArgs()).thenReturn(new Object[0]);
  }

  @AfterEach
  void tearDown() {
    MDC.clear();
    Logger targetLogger = (Logger) LoggerFactory.getLogger(DummyService.class);
    targetLogger.detachAppender(listAppender);
  }

  @Test
  void logMethod_ShouldLogStartAndSuccess() throws Throwable {
    when(joinPoint.proceed()).thenReturn("done");

    Object result = loggingAspect.logMethod(joinPoint);

    assertEquals("done", result);

    List<ILoggingEvent> logs = listAppender.list;

    assertTrue(logs.stream().anyMatch(e -> e.getFormattedMessage().contains("START with args")));
    assertTrue(logs.stream().anyMatch(e -> e.getFormattedMessage().contains("SUCCESS in")));

    assertNull(MDC.get(CORRELATION_ID_KEY));
  }

  @Test
  void logMethod_WhenExceptionOccurs_ShouldLogError() throws Throwable {
    when(joinPoint.proceed()).thenThrow(new RuntimeException("boom"));

    Assertions.assertThrows(RuntimeException.class, () -> loggingAspect.logMethod(joinPoint));

    List<ILoggingEvent> logs = listAppender.list;

    assertTrue(
        logs.stream()
            .anyMatch(
                e ->
                    e.getLevel() == Level.ERROR
                        && e.getFormattedMessage().contains("ERROR in")
                        && e.getFormattedMessage().contains("boom")));

    assertNull(MDC.get(CORRELATION_ID_KEY));
  }

  @Test
  void logMethod_ShouldLogArgsInStartMessage() throws Throwable {
    Object[] args = {"arg1", 42};
    when(joinPoint.getArgs()).thenReturn(args);
    when(joinPoint.proceed()).thenReturn("done");

    loggingAspect.logMethod(joinPoint);

    List<ILoggingEvent> logs = listAppender.list;

    assertTrue(
        logs.stream()
            .anyMatch(
                e ->
                    e.getFormattedMessage().contains("START with args")
                        && e.getFormattedMessage().contains("[arg1, 42]")));
  }

  @Test
  void logMethod_WhenTargetIsNull_UsesSignatureDeclaringType() throws Throwable {
    try (DynamicType.Unloaded<Object> type =
        new ByteBuddy()
            .subclass(Object.class)
            .name("com.company.test.MyBusinessLogMethodClass")
            .defineMethod("getValue", String.class, Visibility.PUBLIC)
            .intercept(FixedValue.value("John"))
            .make()) {

      Class<?> dynamicClass =
          type.load(getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
              .getLoaded();

      Logger dynamicLogger = (Logger) LoggerFactory.getLogger(dynamicClass);
      dynamicLogger.setLevel(Level.DEBUG);
      dynamicLogger.addAppender(listAppender);

      Method m = dynamicClass.getMethod("getValue");

      when(joinPoint.getTarget()).thenReturn(null);
      when(joinPoint.getSignature()).thenReturn(methodSignature);
      when(methodSignature.getDeclaringType()).thenReturn(dynamicClass);
      when(methodSignature.getMethod()).thenReturn(m);
      when(joinPoint.getArgs()).thenReturn(new Object[0]);
      when(joinPoint.proceed()).thenReturn("John");

      Object result = loggingAspect.logMethod(joinPoint);

      assertEquals("John", result);

      assertTrue(
          listAppender.list.stream()
              .anyMatch(e -> e.getFormattedMessage().contains("START with args")));
    }
  }

  @Test
  void logMethod_WhenTargetClassIsNull_ReturnsProceedResult() throws Throwable {
    LoggingAspect aspect = new LoggingAspect(List.of("it.portus.ms.valid.package"));

    when(joinPoint.getTarget()).thenReturn(null);
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getDeclaringType()).thenReturn(null);
    when(joinPoint.proceed()).thenReturn("fromProceed");

    Object result = aspect.logMethod(joinPoint);

    assertEquals("fromProceed", result);
  }
}
