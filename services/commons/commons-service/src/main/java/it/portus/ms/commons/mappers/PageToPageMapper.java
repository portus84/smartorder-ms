package it.portus.ms.commons.mappers;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass({org.springframework.data.domain.Page.class})
public class PageToPageMapper extends AbstractPageMapper {

  public PageToPageMapper(List<PageContentMapper<?, ?>> mappers) {
    super(mappers);
  }

  public <S, T> Page<T> toPage(Page<S> sourcePage, Class<T> targetClass) {
    PageContentMapper<S, T> mapper = resolveMapper(targetClass);
    return sourcePage.map(mapper::toDTO);
  }
}
