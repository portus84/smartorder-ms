package it.portus.smartorder.ms.invservice.business.services;

import it.portus.business.commons.service.CrudService;
import it.portus.smartorder.ms.invservice.business.domain.model.Inventory;
import java.util.UUID;

public interface InventoryService extends CrudService<Inventory, UUID> {

  boolean checkAvailability(String orderId);
}
