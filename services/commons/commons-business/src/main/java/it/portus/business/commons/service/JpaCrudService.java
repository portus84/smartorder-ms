package it.portus.business.commons.service;

import it.portus.business.commons.model.Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class JpaCrudService<T extends Entity<I>, I>
    extends AbstractCrudService<T, I, JpaRepository<T, I>> {

  protected JpaCrudService(JpaRepository<T, I> repository) {
    super(repository);
  }

  @Override
  public Page<T> findAll(Pageable pageable) {
    return repository.findAll(pageable);
  }
}
