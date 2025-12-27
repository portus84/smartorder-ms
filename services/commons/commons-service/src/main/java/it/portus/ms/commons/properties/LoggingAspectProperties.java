package it.portus.ms.commons.properties;

import java.util.Collections;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "logging.aspect")
public class LoggingAspectProperties {

  private List<String> extraPackages = Collections.emptyList();
}
