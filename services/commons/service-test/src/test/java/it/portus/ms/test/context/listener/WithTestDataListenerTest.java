package it.portus.ms.test.context.listener;

import static org.mockito.Mockito.*;

import it.portus.ms.test.annotation.WithTestData;
import it.portus.ms.test.data.TestDataLoader;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@ExtendWith(MockitoExtension.class)
class WithTestDataListenerTest {

  @Mock private TestContext testContext;
  @Mock private ApplicationContext applicationContext;
  @Mock private PlatformTransactionManager txManager;
  @Mock private TransactionStatus txStatus;
  @Mock private TestDataLoader loader;

  private WithTestDataListener listener;

  @BeforeEach
  void setup() {
    listener = new WithTestDataListener();

    lenient().when(testContext.getApplicationContext()).thenReturn(applicationContext);
    lenient().when(applicationContext.getBean(TestDataLoader.class)).thenReturn(loader);
  }

  @SuppressWarnings("unused")
  @WithTestData(value = TestDataLoader.class)
  static class DummyTestClass {
    @WithTestData(value = TestDataLoader.class)
    void transactionalMethod() {}

    @WithTestData(value = TestDataLoader.class, transactional = false)
    void rollbackMethod() {}

    @WithTestData(value = TestDataLoader.class, transactional = false, rollback = false)
    void commitMethod() {}
  }

  @Test
  void beforeTestClass_WhenAnnotationPresentOnClass_ShouldRegisterLoaderBeans() {
    GenericApplicationContext ctx = mock(GenericApplicationContext.class);
    doReturn(DummyTestClass.class).when(testContext).getTestClass();
    when(testContext.getApplicationContext()).thenReturn(ctx);
    when(ctx.containsBeanDefinition(anyString())).thenReturn(false);

    listener.beforeTestClass(testContext);

    verify(testContext, atLeastOnce()).getApplicationContext();
    verify(testContext).getTestClass();
  }

  @Test
  void beforeTestClass_WhenNoAnnotationOnClass_ShouldDoNothing() {
    doReturn(Object.class).when(testContext).getTestClass();

    listener.beforeTestClass(testContext);

    verify(testContext).getTestClass();
  }

  @Test
  void beforeTestMethod_WhenTransactionalTrue_ShouldLoadDataWithoutStartingTransaction()
      throws Exception {
    mockMethod("transactionalMethod");

    listener.beforeTestMethod(testContext);

    verify(loader, times(1)).load();
    verifyNoInteractions(txManager);
  }

  @Test
  void beforeTestMethod_WhenTestMethodNotAvailable_ShouldSearchAnnotationOnClass() {
    when(testContext.getTestMethod()).thenThrow(new IllegalStateException("No test method"));
    doReturn(DummyTestClass.class).when(testContext).getTestClass();

    listener.beforeTestMethod(testContext);

    verify(testContext).getTestMethod();
    verify(testContext).getTestClass();
  }

  @Test
  void beforeTestMethod_WhenTransactionalFalseAndRollbackTrue_ShouldStartTransaction()
      throws Exception {
    mockMethod("rollbackMethod");

    when(applicationContext.getBean(PlatformTransactionManager.class)).thenReturn(txManager);
    when(txManager.getTransaction(any(DefaultTransactionDefinition.class))).thenReturn(txStatus);

    listener.beforeTestMethod(testContext);

    verify(txManager, times(1)).getTransaction(any(TransactionDefinition.class));
    verify(loader, times(1)).load();
  }

  @Test
  void beforeAndAfterTestMethod_WhenTransactionalFalseAndRollbackTrue_ShouldRollbackTransaction()
      throws Exception {
    mockMethod("rollbackMethod");

    when(applicationContext.getBean(PlatformTransactionManager.class)).thenReturn(txManager);
    when(txManager.getTransaction(any(DefaultTransactionDefinition.class))).thenReturn(txStatus);
    when(testContext.getAttribute(any())).thenReturn(txStatus);

    listener.beforeTestMethod(testContext);
    listener.afterTestMethod(testContext);

    verify(txManager, times(1)).getTransaction(any(TransactionDefinition.class));
    verify(loader, times(1)).load();
    verify(txManager, times(1)).rollback(txStatus);
    verify(txManager, never()).commit(any());
  }

  @Test
  void beforeAndAfterTestMethod_WhenTransactionalFalseAndRollbackFalse_ShouldCommitTransaction()
      throws Exception {
    mockMethod("commitMethod");

    when(applicationContext.getBean(PlatformTransactionManager.class)).thenReturn(txManager);
    when(txManager.getTransaction(any(DefaultTransactionDefinition.class))).thenReturn(txStatus);
    when(testContext.getAttribute(any())).thenReturn(txStatus);

    listener.beforeTestMethod(testContext);
    listener.afterTestMethod(testContext);

    verify(txManager, times(1)).getTransaction(any(TransactionDefinition.class));
    verify(loader, times(1)).load();
    verify(txManager, times(1)).commit(txStatus);
    verify(txManager, never()).rollback(any());
  }

  @Test
  void afterTestMethod_WhenTransactionalTrue_ShouldNotCommitOrRollback() throws Exception {
    mockMethod("transactionalMethod");

    listener.afterTestMethod(testContext);

    verifyNoInteractions(txManager);
  }

  @Test
  void afterTestMethod_WhenNoTransactionStatusAttribute_ShouldDoNothing() throws Exception {
    mockMethod("rollbackMethod");

    when(applicationContext.getBean(PlatformTransactionManager.class)).thenReturn(txManager);
    when(testContext.getAttribute(any())).thenReturn(null);

    listener.afterTestMethod(testContext);

    verify(txManager, never()).rollback(any());
    verify(txManager, never()).commit(any());
  }

  private void mockMethod(String methodName) throws NoSuchMethodException {
    Method method = DummyTestClass.class.getDeclaredMethod(methodName);
    when(testContext.getTestMethod()).thenReturn(method);
  }
}
