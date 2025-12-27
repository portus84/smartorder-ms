package it.portus.ms.commons.config;

import it.portus.ms.commons.hateoas.PluralizingRelProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilderFactory;

@AutoConfiguration
@ConditionalOnClass(org.springframework.hateoas.config.HateoasConfiguration.class)
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL_FORMS)
public class HateoasConfiguration {

  @Bean
  public LinkRelationProvider relProvider() {
    return new PluralizingRelProvider();
  }

  @Bean
  public WebMvcLinkBuilderFactory linkBuilderFactory() {
    return new WebMvcLinkBuilderFactory();
  }
}
