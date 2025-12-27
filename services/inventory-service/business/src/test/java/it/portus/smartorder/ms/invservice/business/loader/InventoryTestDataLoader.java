package it.portus.smartorder.ms.invservice.business.loader;

import it.portus.ms.test.data.TestDataLoader;
import it.portus.smartorder.ms.invservice.business.domain.model.Inventory;
import it.portus.smartorder.ms.invservice.business.domain.repositories.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class InventoryTestDataLoader implements TestDataLoader {

  private final InventoryRepository inventoryRepository;

  @Override
  public void load() {
    inventoryRepository.saveAll(
        Instancio.ofList(Inventory.class)
            .size(5)
            .ignore(Select.field(Inventory::getId))
            .ignore(Select.field(Inventory::getCreatedDate))
            .ignore(Select.field(Inventory::getLastModifiedDate))
            .create());
  }
}
