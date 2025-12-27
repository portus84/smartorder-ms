package it.portus.ms.test.context.listener;

import it.portus.ms.test.annotation.WithTestData;
import it.portus.ms.test.data.TestDataLoader;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Slf4j
public class WithTestDataListener extends AbstractTestExecutionListener {
  private static final String TX_STATUS_KEY = "WithTestDataListener.TX_STATUS";
  private static final String LOADERS_REGISTERED_KEY = "WithTestDataListener.LOADERS_REGISTERED";

  @Override
  public void beforeTestClass(TestContext testContext) {
    findAnnotationOnClass(testContext)
        .ifPresent(
            annotation -> {
              final ApplicationContext ctx = testContext.getApplicationContext();

              registerLoaderBeans(ctx, annotation);
              testContext.setAttribute(LOADERS_REGISTERED_KEY, true);
            });
  }

  @Override
  public void beforeTestMethod(TestContext testContext) {
    findAnnotation(testContext)
        .ifPresent(
            annotation -> {
              final ApplicationContext ctx = testContext.getApplicationContext();
              Runnable loadData = () -> loadTestData(ctx, annotation);
              if (annotation.transactional()) {
                loadData.run();
                return;
              }
              PlatformTransactionManager txManager = ctx.getBean(PlatformTransactionManager.class);
              DefaultTransactionDefinition def = new DefaultTransactionDefinition();
              def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
              TransactionStatus status = txManager.getTransaction(def);
              testContext.setAttribute(TX_STATUS_KEY, status);
              loadData.run();
            });
  }

  @Override
  public void afterTestMethod(TestContext testContext) {
    findAnnotation(testContext)
        .filter(annotation -> !annotation.transactional())
        .ifPresent(
            annotation -> {
              final ApplicationContext ctx = testContext.getApplicationContext();
              final PlatformTransactionManager txManager =
                  ctx.getBean(PlatformTransactionManager.class);
              Optional.ofNullable(testContext.getAttribute(TX_STATUS_KEY))
                  .map(TransactionStatus.class::cast)
                  .ifPresent(
                      status -> {
                        if (annotation.rollback()) {
                          txManager.rollback(status);
                        } else {
                          txManager.commit(status);
                        }
                      });
            });
  }

  private Optional<WithTestData> findAnnotationOnClass(TestContext testContext) {
    return Optional.ofNullable(
        AnnotatedElementUtils.findMergedAnnotation(testContext.getTestClass(), WithTestData.class));
  }

  private Optional<WithTestData> findAnnotation(TestContext testContext) {
    try {
      testContext.getTestMethod();
      Optional<WithTestData> methodAnnotation =
          Optional.ofNullable(
              AnnotatedElementUtils.findMergedAnnotation(
                  testContext.getTestMethod(), WithTestData.class));
      if (methodAnnotation.isPresent()) {
        return methodAnnotation;
      }
    } catch (IllegalStateException e) {
      log.warn(
          "Test method not available during beforeTestClass, searching annotation on class only");
    }

    return Optional.ofNullable(
        AnnotatedElementUtils.findMergedAnnotation(testContext.getTestClass(), WithTestData.class));
  }

  private void registerLoaderBeans(ApplicationContext ctx, WithTestData annotation) {
    if (ctx instanceof BeanDefinitionRegistry registry) {
      Arrays.stream(annotation.value())
          .forEach(
              loaderClass -> {
                String beanName = loaderClass.getSimpleName();
                if (!registry.containsBeanDefinition(beanName)) {
                  BeanDefinition beanDef = new RootBeanDefinition(loaderClass);
                  registry.registerBeanDefinition(beanName, beanDef);
                }
              });
    }
  }

  private void loadTestData(ApplicationContext ctx, WithTestData annotation) {
    Arrays.stream(annotation.value())
        .flatMap(
            type -> {
              try {
                return Optional.of((TestDataLoader) ctx.getBean(type)).stream();
              } catch (Exception e) {
                throw new IllegalStateException(
                    "Missing TestDataLoader bean for class: " + type.getName(), e);
              }
            })
        .forEach(TestDataLoader::load);
  }
}
