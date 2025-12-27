package it.portus.business.commons.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.portus.business.commons.model.Entity;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

@ExtendWith(MockitoExtension.class)
class AbstractCrudServiceTest {

  private AbstractCrudService<TestEntity, String, CrudRepository<TestEntity, String>> service;

  @Mock private CrudRepository<TestEntity, String> repository;

  @BeforeEach
  void setUp() {
    service =
        new AbstractCrudService<>(repository) {
          @Override
          public Page<TestEntity> findAll(Pageable pageable) {
            return Page.empty();
          }
        };
  }

  @Test
  void save_newEntity_createsEntityWithId() {
    TestEntity entity = Instancio.of(TestEntity.class).create();
    when(repository.save(entity)).thenReturn(entity);

    TestEntity saved = service.save(entity);

    assertNotNull(saved);
    verify(repository, times(1)).save(entity);
  }

  @Test
  void update_existingEntity_savesUpdatedEntity() {
    String entityId = "existing-id";

    TestEntity existing =
        Instancio.of(TestEntity.class)
            .set(Select.field(TestEntity::getId), entityId)
            .set(Select.field(TestEntity::getName), "Original Name")
            .create();

    TestEntity entityWithUpdates =
        Instancio.of(TestEntity.class)
            .set(Select.field(TestEntity::getId), entityId)
            .set(Select.field(TestEntity::getName), "Updated Name")
            .create();

    when(repository.findById(entityId)).thenReturn(Optional.of(existing));

    when(repository.save(any(TestEntity.class)))
        .thenAnswer(
            invocation -> {
              TestEntity savedEntity = invocation.getArgument(0);
              assertEquals(entityWithUpdates.getId(), savedEntity.getId());
              return savedEntity;
            });

    Optional<TestEntity> updated = service.update(entityId, entityWithUpdates);

    assertTrue(updated.isPresent());
    assertEquals(entityId, updated.get().getId());

    verify(repository, times(1)).findById(entityId);
    verify(repository, times(1)).save(any(TestEntity.class));
  }

  @Test
  void update_nonExistingEntity_returnsEmpty() {
    String nonexistentId = "nonexistent-id";
    TestEntity entity = Instancio.of(TestEntity.class).create();

    when(repository.findById(nonexistentId)).thenReturn(Optional.empty());

    Optional<TestEntity> result = service.update(nonexistentId, entity);

    assertFalse(result.isPresent());
    verify(repository, times(1)).findById(nonexistentId);
    verify(repository, never()).save(any(TestEntity.class));
  }

  @Test
  void saveAll_multipleEntities_createsAllEntitiesWithIds() {
    List<TestEntity> entities = Instancio.ofList(TestEntity.class).size(3).create();
    when(repository.saveAll(entities)).thenReturn(entities);

    Iterable<TestEntity> saved = service.saveAll(entities);

    assertEquals(3, ((List<TestEntity>) saved).size());
    verify(repository, times(1)).saveAll(entities);
  }

  @Test
  void findById_existingEntity_returnsEntity() {
    TestEntity entity = Instancio.of(TestEntity.class).create();
    when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

    Optional<TestEntity> result = service.findById(entity.getId());

    assertTrue(result.isPresent());
    assertEquals(entity.getId(), result.get().getId());
    verify(repository, times(1)).findById(entity.getId());
  }

  @Test
  void findById_nonExistingEntity_returnsEmpty() {
    when(repository.findById("nonexistent-id")).thenReturn(Optional.empty());

    Optional<TestEntity> result = service.findById("nonexistent-id");

    assertFalse(result.isPresent());
    verify(repository, times(1)).findById("nonexistent-id");
  }

  @Test
  void delete_existingEntity_removesEntity() {
    TestEntity entity = Instancio.of(TestEntity.class).create();

    service.delete(entity);

    verify(repository, times(1)).delete(entity);
  }

  @Test
  void deleteById_existingEntity_removesEntity() {
    String id = "some-id";

    service.deleteById(id);

    verify(repository, times(1)).deleteById(id);
  }

  @Data
  static class TestEntity implements Entity<String> {
    private String id;
    private String name;
  }
}
