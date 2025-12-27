package it.portus.smartorder.ms.invservice.bootstrap.conf;

import it.portus.smartorder.ms.invservice.business.conf.BusinessAutoConfiguration;
import it.portus.smartorder.ms.invservice.business.domain.model.Inventory;
import it.portus.smartorder.ms.invservice.business.services.InventoryService;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureAfter(BusinessAutoConfiguration.class)
@ConditionalOnClass({BusinessAutoConfiguration.class, Instancio.class})
public class TestDataLoaderConfiguration {

  @Bean
  CommandLineRunner loadTestData(InventoryService service) {
    return args ->
        service.saveAll(
            Instancio.ofList(Inventory.class)
                .size(5)
                .ignore(Select.field(Inventory::getId))
                .ignore(Select.field(Inventory::getCreatedDate))
                .ignore(Select.field(Inventory::getLastModifiedDate))
                .create());
  }
}
