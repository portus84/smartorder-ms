package it.portus.business.commons.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.portus.business.commons.model.Entity;
import java.util.List;
import lombok.Data;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

@ExtendWith(MockitoExtension.class)
class JpaCrudServiceTest {

  private JpaCrudService<TestEntity, String> service;

  @Mock private JpaRepository<TestEntity, String> repository;

  @BeforeEach
  void setUp() {
    service = new JpaCrudService<>(repository) {};
  }

  @Test
  void findAll_withPageable_callsRepositoryAndReturnsPage() {
    List<TestEntity> entities = Instancio.ofList(TestEntity.class).size(3).create();
    Page<TestEntity> page = new PageImpl<>(entities);

    when(repository.findAll(Pageable.unpaged())).thenReturn(page);

    Page<TestEntity> result = service.findAll(Pageable.unpaged());

    assertEquals(entities.size(), result.getTotalElements());
    verify(repository, times(1)).findAll(Pageable.unpaged());
  }

  @Data
  static class TestEntity implements Entity<String> {
    private String id;
    private String name;
  }
}
