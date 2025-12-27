package it.portus.smartorder.ms.invservice.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.HateoasAwareSpringDataWebConfiguration;

@SpringBootApplication
@ImportAutoConfiguration(classes = {HateoasAwareSpringDataWebConfiguration.class})
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
