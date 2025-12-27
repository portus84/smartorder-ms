package it.portus.smartorder.ms.orderservice.bootstrap.conf;

import it.portus.smartorder.ms.orderservice.business.conf.BusinessAutoConfiguration;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.services.OrderService;
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
  CommandLineRunner loadTestData(OrderService service) {
    return args ->
        service.saveAll(
            Instancio.ofList(Order.class)
                .size(5)
                .ignore(
                    Select.all(
                        Select.field(Order::getId),
                        Select.field(Order::getCreatedDate),
                        Select.field(Order::getLastModifiedDate),
                        Select.field(Order::getState)))
                .create());
  }
}
