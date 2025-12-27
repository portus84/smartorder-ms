package it.portus.smartorder.ms.orderservice.api.config;

import it.portus.ms.commons.mappers.ObjectIdMapperImpl;
import it.portus.smartorder.ms.orderservice.api.mappers.OrderMapperImpl;
import it.portus.smartorder.ms.orderservice.api.mappers.OrderStateMapperImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
  ObjectIdMapperImpl.class,
  OrderMapperImpl.class,
  OrderStateMapperImpl.class,
})
public class MappersConfig {}
