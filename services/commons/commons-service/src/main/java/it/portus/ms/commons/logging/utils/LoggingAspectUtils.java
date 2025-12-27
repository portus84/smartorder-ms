package it.portus.ms.commons.logging.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.jspecify.annotations.NonNull;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;

@UtilityClass
public class LoggingAspectUtils {

  private static final List<String> EXCLUDED_PACKAGE_KEYWORDS =
      List.of(
          "org.springframework",
          "jakarta.",
          "java.",
          "jdk.",
          "com.sun.",
          "com.fasterxml.jackson",
          "jackson.",
          "micrometer.",
          "reactor.",
          "tools.jackson",
          "ms.commons");

  @SuppressWarnings("unchecked")
  public static Optional<Class<?>> getTargetClass(ProceedingJoinPoint joinPoint) {
    Class<?> targetClass =
        Optional.ofNullable(joinPoint.getTarget())
            .map(AopProxyUtils::ultimateTargetClass)
            .orElseGet(
                () -> {
                  Signature signature = joinPoint.getSignature();
                  return signature != null ? signature.getDeclaringType() : null;
                });

    return Optional.ofNullable(targetClass);
  }

  public static Class<?> getLoggerClass(@NonNull Class<?> targetClass) {
    return Arrays.stream(targetClass.getInterfaces()).findFirst().orElse(targetClass);
  }

  public static boolean isBusinessClass(Class<?> clazz) {
    String className = clazz.getName();
    boolean excludedByKeyword = EXCLUDED_PACKAGE_KEYWORDS.stream().anyMatch(className::contains);

    return !excludedByKeyword
        && !AnnotatedElementUtils.hasAnnotation(clazz, Configuration.class)
        && !AnnotatedElementUtils.hasAnnotation(clazz, AutoConfiguration.class)
        && !AopInfrastructureBean.class.isAssignableFrom(clazz)
        && !clazz.isSynthetic();
  }

  public static String formatArgs(Object[] args) {
    return Optional.ofNullable(args)
        .filter(a -> a.length > 0)
        .map(
            a ->
                Arrays.stream(a)
                    .map(obj -> Optional.ofNullable(obj).map(Object::toString).orElse("null"))
                    .collect(Collectors.joining(", ", "[", "]")))
        .orElse("[]");
  }
}
