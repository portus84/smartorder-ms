package it.portus.smartorder.ms.invservice.api.config;

import it.portus.ms.commons.config.HateoasConfiguration;
import it.portus.ms.commons.config.JacksonAutoConfiguration;
import it.portus.ms.commons.handlers.RestResponseEntityExceptionHandler;
import it.portus.ms.commons.mappers.PageToPageMapper;
import it.portus.ms.commons.mappers.PageToPagedModelMapper;
import it.portus.smartorder.ms.invservice.api.hateoas.HateoasInventoryHelper;
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
  HateoasInventoryHelper.class
})
public class ControllerTestConfig {}
