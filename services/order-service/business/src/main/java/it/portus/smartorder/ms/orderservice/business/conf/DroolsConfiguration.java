package it.portus.smartorder.ms.orderservice.business.conf;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(KieContainer.class)
public class DroolsConfiguration {

  private static final KieServices kieServices = KieServices.Factory.get();

  @Bean
  public KieContainer kieContainer() {
    return kieServices.newKieClasspathContainer(this.getClass().getClassLoader());
  }

  @Bean
  public KieBase orderKieBase(KieContainer kieContainer) {
    return kieContainer.getKieBase("OrderKieBase");
  }
}
