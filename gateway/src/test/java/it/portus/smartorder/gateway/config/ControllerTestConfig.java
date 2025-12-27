package it.portus.smartorder.gateway.config;

import it.portus.ms.commons.handlers.ReactiveResponseEntityExceptionHandler;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({ReactiveResponseEntityExceptionHandler.class})
public class ControllerTestConfig {}
