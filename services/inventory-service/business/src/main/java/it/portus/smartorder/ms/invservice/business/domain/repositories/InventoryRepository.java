package it.portus.smartorder.ms.invservice.business.domain.repositories;

import it.portus.smartorder.ms.invservice.business.domain.model.Inventory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {}
