package it.portus.ms.commons.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.portus.ms.commons.jackson.module.EntityModelModule;
import it.portus.ms.commons.jackson.module.PageModule;
import it.portus.ms.commons.jackson.module.PageableModule;
import it.portus.ms.commons.jackson.module.PagedModelModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

@AutoConfiguration
public class JacksonAutoConfiguration {

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
    return builder ->
        builder.featuresToDisable(
            com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @Bean
  public Module javaTimeModule() {
    return new JavaTimeModule();
  }

  @Bean
  @Primary
  @ConditionalOnClass(Page.class)
  public Module pagedPageModule() {
    return new PageModule();
  }

  @Bean
  @ConditionalOnClass(Pageable.class)
  public Module pageableModule() {
    return new PageableModule();
  }

  @Bean
  @ConditionalOnClass(EntityModel.class)
  public Module entityModelModule() {
    return new EntityModelModule();
  }

  @Bean
  @ConditionalOnClass(PagedModel.class)
  public Module pagedModelModule() {
    return new PagedModelModule();
  }
}
