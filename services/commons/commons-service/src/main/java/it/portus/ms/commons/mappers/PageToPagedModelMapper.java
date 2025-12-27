package it.portus.ms.commons.mappers;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass({org.springframework.hateoas.PagedModel.class})
public class PageToPagedModelMapper extends AbstractPageMapper {

  public PageToPagedModelMapper(List<PageContentMapper<?, ?>> mappers) {
    super(mappers);
  }

  public <S, T> PagedModel<EntityModel<T>> toPagedModel(Page<S> sourcePage, Class<T> targetClass) {
    return toPagedModel(sourcePage, targetClass, new Link[] {});
  }

  public <S, T> PagedModel<EntityModel<T>> toPagedModel(
      Page<S> sourcePage, Class<T> targetClass, Link... links) {

    PageContentMapper<S, T> mapper = resolveMapper(targetClass);

    List<EntityModel<T>> content =
        sourcePage.getContent().stream()
            .map(mapper::toDTO)
            .map(EntityModel::of)
            .collect(Collectors.toList());

    PageMetadata metadata =
        new PageMetadata(
            sourcePage.getSize(),
            sourcePage.getNumber(),
            sourcePage.getTotalElements(),
            sourcePage.getTotalPages());

    return PagedModel.of(content, metadata, links);
  }
}
