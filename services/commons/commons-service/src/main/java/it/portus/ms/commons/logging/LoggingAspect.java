package it.portus.ms.commons.logging;

import it.portus.ms.commons.logging.utils.LoggingAspectUtils;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Slf4j
@Aspect
public class LoggingAspect {

  private final List<String> basePackages;
  private static final String CORRELATION_ID = "correlationId";

  public LoggingAspect() {
    this.basePackages = Collections.emptyList();
  }

  public LoggingAspect(List<String> basePackages) {
    this.basePackages = List.copyOf(basePackages);
  }

  @SuppressWarnings("java:S2139") // Logging before rethrow is intentional
  @Around("execution(* it.portus..*(..))" + " && !within(it.portus.ms.commons..*)")
  public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    Optional<Class<?>> optTargetClass = LoggingAspectUtils.getTargetClass(joinPoint);
    Class<?> targetClass = optTargetClass.orElse(null);

    if (targetClass == null
        || !shouldLogFor(targetClass) && !LoggingAspectUtils.isBusinessClass(targetClass)) {
      return joinPoint.proceed();
    }

    Logger targetLogger = LoggerFactory.getLogger(LoggingAspectUtils.getLoggerClass(targetClass));
    String methodName = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();

    String correlationId = getOrGenerateCorrelationId();
    long start = System.currentTimeMillis();

    if (targetLogger.isDebugEnabled()) {
      targetLogger.debug(
          "[correlationId: {}] {} - START with args: {}",
          correlationId,
          methodName,
          LoggingAspectUtils.formatArgs(joinPoint.getArgs()));
    }

    try {
      Object result = joinPoint.proceed();
      long duration = System.currentTimeMillis() - start;

      targetLogger.info(
          "[correlationId: {}] {} - SUCCESS in {} ms, result: {}",
          correlationId,
          methodName,
          duration,
          result);

      return result;
    } catch (Exception ex) {
      long duration = System.currentTimeMillis() - start;

      targetLogger.error(
          "[correlationId: {}] {} - ERROR in {} ms: {}",
          correlationId,
          methodName,
          duration,
          ex.getMessage(),
          ex);

      throw ex;
    } finally {
      MDC.remove(CORRELATION_ID);
    }
  }

  protected boolean shouldLogFor(Class<?> targetClass) {
    return basePackages.stream().anyMatch(targetClass.getPackageName()::startsWith);
  }

  private static String getOrGenerateCorrelationId() {
    return Optional.ofNullable(MDC.get(CORRELATION_ID))
        .orElseGet(
            () -> {
              String newId = UUID.randomUUID().toString();
              MDC.put(CORRELATION_ID, newId);
              return newId;
            });
  }
}
