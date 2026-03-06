package it.portus.ms.commons.mappers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

@ExtendWith(MockitoExtension.class)
class PageToPagedModelMapperTest {

  static class Source {}

  static class Target {}

  @Mock private PageContentMapper<Source, Target> behaviorMapperMock;

  interface TargetMapper extends PageContentMapper<Source, Target> {}

  class TargetMapperImpl implements TargetMapper {

    @Override
    public Target toDTO(Source entity) {
      return behaviorMapperMock.toDTO(entity);
    }
  }

  static class UnmappedTarget {}

  private PageToPagedModelMapper pageToPagedModelMapper;

  @BeforeEach
  void setUp() {
    TargetMapperImpl targetMapper = new TargetMapperImpl();

    PageContentMapper<?, ?> otherMapper = mock(PageContentMapper.class);

    List<PageContentMapper<?, ?>> mappers = List.of(targetMapper, otherMapper);
    pageToPagedModelMapper = new PageToPagedModelMapper(mappers);
  }

  @Test
  void toPagedModel_WhenMapperIsFound_MapsPageContentAndMetadata() {
    Source sourceElement = new Source();
    List<Source> sourceList = List.of(sourceElement);
    Page<Source> sourcePage = new PageImpl<>(sourceList, Pageable.unpaged(), 1);

    Target targetElement = new Target();

    when(behaviorMapperMock.toDTO(sourceElement)).thenReturn(targetElement);

    PagedModel<EntityModel<Target>> result =
        pageToPagedModelMapper.toPagedModel(sourcePage, Target.class);

    assertNotNull(result, "The resulting PagedModel should not be null.");
    assertEquals(1, result.getContent().size(), "The PagedModel should contain one element.");

    EntityModel<Target> entityModel = result.getContent().iterator().next();
    assertEquals(
        targetElement, entityModel.getContent(), "The mapped element should be the expected one.");

    PagedModel.PageMetadata metadata = result.getMetadata();
    assertNotNull(metadata, "Metadata should not be null.");
    assertEquals(1, metadata.getTotalElements(), "Total elements should match the source page.");
    assertEquals(1, metadata.getTotalPages(), "Total pages should match the source page.");
    assertEquals(0, metadata.getNumber(), "Page number should match the source page.");

    verify(behaviorMapperMock, times(1)).toDTO(sourceElement);
  }

  @Test
  void toPagedModel_WhenMapperIsNotFound_ThrowsException() {
    Page<Source> sourcePage = new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 0);

    assertThrows(
        IllegalArgumentException.class,
        () -> pageToPagedModelMapper.toPagedModel(sourcePage, UnmappedTarget.class),
        "Should throw an IllegalArgumentException when the mapper is not found.");

    verify(behaviorMapperMock, never()).toDTO(any());
  }
}
