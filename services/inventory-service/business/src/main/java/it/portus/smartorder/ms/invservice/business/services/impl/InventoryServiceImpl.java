package it.portus.smartorder.ms.invservice.business.services.impl;

import it.portus.business.commons.service.JpaCrudService;
import it.portus.smartorder.ms.invservice.business.domain.model.Inventory;
import it.portus.smartorder.ms.invservice.business.domain.repositories.InventoryRepository;
import it.portus.smartorder.ms.invservice.business.services.InventoryService;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class InventoryServiceImpl extends JpaCrudService<Inventory, UUID>
    implements InventoryService {

  public InventoryServiceImpl(InventoryRepository repository) {
    super(repository);
  }
}
