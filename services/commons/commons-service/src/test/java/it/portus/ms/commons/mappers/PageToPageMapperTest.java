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

@ExtendWith(MockitoExtension.class)
class PageToPageMapperTest {

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

  private PageToPageMapper pageToPageMapper;

  @BeforeEach
  void setUp() {
    TargetMapperImpl targetMapper = new TargetMapperImpl();

    PageContentMapper<?, ?> otherMapper = mock(PageContentMapper.class);

    List<PageContentMapper<?, ?>> mappers = List.of(targetMapper, otherMapper);
    pageToPageMapper = new PageToPageMapper(mappers);
  }

  @Test
  void toPage_WhenMapperIsFound_MapsPageContent() {
    Source sourceElement = new Source();
    List<Source> sourceList = List.of(sourceElement);
    Page<Source> sourcePage = new PageImpl<>(sourceList, Pageable.unpaged(), 1);

    Target targetElement = new Target();

    when(behaviorMapperMock.toDTO(sourceElement)).thenReturn(targetElement);

    Page<Target> resultPage = pageToPageMapper.toPage(sourcePage, Target.class);

    assertNotNull(resultPage, "The resulting page should not be null.");
    assertEquals(1, resultPage.getContent().size(), "The page should contain one element.");
    assertEquals(
        targetElement,
        resultPage.getContent().getFirst(),
        "The mapped element should be the expected one.");

    verify(behaviorMapperMock, times(1)).toDTO(sourceElement);
  }

  @Test
  void toPage_WhenMapperIsNotFound_ThrowsException() {
    Page<Source> sourcePage = new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 0);

    assertThrows(
        IllegalArgumentException.class,
        () -> pageToPageMapper.toPage(sourcePage, UnmappedTarget.class),
        "Should throw an IllegalArgumentException when the mapper is not found.");

    verify(behaviorMapperMock, never()).toDTO(any());
  }
}
