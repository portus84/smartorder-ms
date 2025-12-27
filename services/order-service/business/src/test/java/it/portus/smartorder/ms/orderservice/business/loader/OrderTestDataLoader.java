package it.portus.smartorder.ms.orderservice.business.loader;

import it.portus.ms.test.data.TestDataLoader;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import it.portus.smartorder.ms.orderservice.business.domain.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class OrderTestDataLoader implements TestDataLoader {

  private final OrderRepository orderRepository;

  @Override
  public void load() {
    orderRepository.saveAll(
        Instancio.ofList(Order.class)
            .size(5)
            .ignore(Select.field(Order::getId))
            .ignore(Select.field(Order::getCreatedDate))
            .ignore(Select.field(Order::getLastModifiedDate))
            .create());
  }
}
