package it.portus.ms.commons.config;

import it.portus.ms.commons.logging.LoggingAspect;
import it.portus.ms.commons.properties.LoggingAspectProperties;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.AnnotationUtils;

@AutoConfiguration
@EnableAspectJAutoProxy
@ConditionalOnClass(Aspect.class)
@EnableConfigurationProperties(LoggingAspectProperties.class)
public class LoggingAspectAutoConfiguration {

  @Bean
  public LoggingAspect loggingAspect(
      ApplicationContext context, LoggingAspectProperties properties) {
    return new LoggingAspect(
        getBasePackages(
            context,
            Optional.ofNullable(properties)
                .map(LoggingAspectProperties::getExtraPackages)
                .orElse(Collections.emptyList())));
  }

  private static List<String> getBasePackages(
      ApplicationContext context, List<String> extraPackages) {
    Predicate<String> isNotExcluded =
        pkg -> getComponentScanPackages(CommonsAutoConfiguration.class).noneMatch(pkg::startsWith);

    Set<String> basePackages = new HashSet<>();
    basePackages.addAll(scanSpringBootApplicationPackages(context, isNotExcluded));
    basePackages.addAll(scanComponentScanPackages(context, isNotExcluded));

    Optional.ofNullable(extraPackages).ifPresent(basePackages::addAll);

    return List.copyOf(basePackages);
  }

  private static Set<String> scanSpringBootApplicationPackages(
      ApplicationContext context, Predicate<String> packageFilter) {
    return context.getBeansWithAnnotation(SpringBootApplication.class).values().stream()
        .map(bean -> bean.getClass().getPackageName())
        .filter(packageFilter)
        .collect(java.util.stream.Collectors.toSet());
  }

  private static Set<String> scanComponentScanPackages(
      ApplicationContext context, Predicate<String> packageFilter) {
    return Arrays.stream(context.getBeanNamesForAnnotation(Configuration.class))
        .map(context::getType)
        .filter(Objects::nonNull)
        .flatMap(LoggingAspectAutoConfiguration::getComponentScanPackages)
        .filter(packageFilter)
        .collect(java.util.stream.Collectors.toSet());
  }

  private static Stream<String> getComponentScanPackages(Class<?> clazz) {
    return Optional.ofNullable(AnnotationUtils.findAnnotation(clazz, ComponentScan.class)).stream()
        .flatMap(x -> Arrays.stream(x.basePackages()));
  }
}
