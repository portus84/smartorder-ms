package it.portus.smartorder.ms.invservice.api.config;

import it.portus.smartorder.ms.invservice.api.mappers.InventoryMapperImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({InventoryMapperImpl.class})
public class MappersConfig {}
