package it.portus.smartorder.ms.orderservice.api.config;

import it.portus.ms.commons.config.HateoasConfiguration;
import it.portus.ms.commons.config.JacksonAutoConfiguration;
import it.portus.ms.commons.handlers.RestResponseEntityExceptionHandler;
import it.portus.ms.commons.mappers.PageToPageMapper;
import it.portus.ms.commons.mappers.PageToPagedModelMapper;
import it.portus.smartorder.ms.orderservice.api.hateoas.HateoasOrderHelper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
  JacksonAutoConfiguration.class,
  HateoasConfiguration.class,
  RestResponseEntityExceptionHandler.class,
  PageToPageMapper.class,
  PageToPagedModelMapper.class,
  MappersConfig.class,
  HateoasOrderHelper.class
})
public class ControllerTestConfig {}
