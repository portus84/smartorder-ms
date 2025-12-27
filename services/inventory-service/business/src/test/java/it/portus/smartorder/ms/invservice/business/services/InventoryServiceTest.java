package it.portus.smartorder.ms.invservice.business.services;

import static org.junit.jupiter.api.Assertions.*;

import it.portus.ms.test.annotation.WithTestData;
import it.portus.smartorder.ms.invservice.business.conf.JpaAutoConfiguration;
import it.portus.smartorder.ms.invservice.business.domain.model.Inventory;
import it.portus.smartorder.ms.invservice.business.domain.repositories.InventoryRepository;
import it.portus.smartorder.ms.invservice.business.loader.InventoryTestDataLoader;
import it.portus.smartorder.ms.invservice.business.services.impl.InventoryServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@WithTestData(InventoryTestDataLoader.class)
@ImportAutoConfiguration(JpaAutoConfiguration.class)
@Import({InventoryServiceImpl.class})
class InventoryServiceTest {

  @Autowired private InventoryRepository inventoryRepository;

  @Autowired private InventoryService inventoryService;

  @Test
  void findAll_withInventories_returnsNonEmptyPage() {
    Page<Inventory> page = inventoryService.findAll(Pageable.unpaged());

    assertNotNull(page);
    assertFalse(page.isEmpty());
    assertTrue(page.getTotalElements() > 0);
  }

  @Test
  void findAll_noInventories_returnsEmptyPage() {
    inventoryRepository.deleteAll();

    Page<Inventory> page = inventoryService.findAll(Pageable.unpaged());

    assertNotNull(page);
    assertTrue(page.isEmpty());
    assertEquals(0, page.getTotalElements());
  }

  @Test
  void findById_existingInventory_returnsInventory() {
    Inventory existing = newInventories().getFirst();
    existing = inventoryRepository.save(existing);

    Optional<Inventory> result = inventoryService.findById(existing.getId());

    assertTrue(result.isPresent());
    assertEquals(existing.getId(), result.get().getId());
  }

  @Test
  void findById_nonExistingInventory_returnsEmpty() {
    Optional<Inventory> result = inventoryService.findById(UUID.randomUUID());

    assertFalse(result.isPresent());
  }

  @Test
  void save_newInventory_createsInventoryWithId() {
    Inventory inventory = newInventory();

    Inventory saved = inventoryService.save(inventory);

    assertNotNull(saved);
    assertNotNull(saved.getId());
  }

  @Test
  void save_existingInventory_updatesInventory() {
    Inventory existing = newInventory();
    existing = inventoryRepository.save(existing);

    existing.setDescription("Updated Description");
    Inventory updated = inventoryService.save(existing);

    assertEquals(existing.getId(), updated.getId());
    assertEquals("Updated Description", updated.getDescription());
  }

  @Test
  void delete_existingInventory_removesInventory() {
    Inventory existing = newInventory();
    existing = inventoryRepository.save(existing);

    inventoryService.delete(existing);

    Optional<Inventory> result = inventoryRepository.findById(existing.getId());
    assertFalse(result.isPresent());
  }

  @Test
  void deleteById_existingInventory_removesInventory() {
    Inventory existing = newInventory();
    existing = inventoryRepository.save(existing);

    inventoryService.deleteById(existing.getId());

    Optional<Inventory> result = inventoryRepository.findById(existing.getId());
    assertFalse(result.isPresent());
  }

  @Test
  void saveAll_multipleNewInventories_createsAllInventoriesWithIds() {
    var inventorys = newInventories();

    Iterable<Inventory> saved = inventoryService.saveAll(inventorys);

    assertNotNull(saved);
    saved.forEach(inventory -> assertNotNull(inventory.getId(), "Saved inventory must have an ID"));

    long countInDb = inventoryRepository.count();
    assertTrue(countInDb >= 3, "Repository should contain at least 3 inventories");
  }

  @Test
  void saveAll_existingInventories_updatesAllInventories() {
    var existingInventories = newInventories();
    existingInventories = inventoryRepository.saveAll(existingInventories);

    existingInventories.forEach(o -> o.setDescription("Updated Description"));

    Iterable<Inventory> updated = inventoryService.saveAll(existingInventories);

    updated.forEach(o -> assertEquals("Updated Description", o.getDescription()));

    updated.forEach(
        o -> {
          Optional<Inventory> fromDb = inventoryRepository.findById(o.getId());
          assertTrue(fromDb.isPresent());
          assertEquals("Updated Description", fromDb.get().getDescription());
        });
  }

  @Test
  void saveAll_emptyList_returnsEmptyIterable() {
    Iterable<Inventory> saved = inventoryService.saveAll(List.of());

    assertNotNull(saved);
    assertFalse(saved.iterator().hasNext(), "Saving empty list should return empty iterable");
  }

  private Inventory newInventory() {
    return Instancio.of(Inventory.class)
        .ignore(Select.field(Inventory::getId))
        .ignore(Select.field(Inventory::getCreatedDate))
        .ignore(Select.field(Inventory::getLastModifiedDate))
        .create();
  }

  private List<Inventory> newInventories() {
    return Instancio.ofList(Inventory.class)
        .ignore(Select.field(Inventory::getId))
        .ignore(Select.field(Inventory::getCreatedDate))
        .ignore(Select.field(Inventory::getLastModifiedDate))
        .create();
  }
}
