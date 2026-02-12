package it.portus.business.commons.service;

import it.portus.business.commons.model.Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public abstract class MongoCrudService<T extends Entity<I>, I>
    extends AbstractCrudService<T, I, MongoRepository<T, I>> {

  protected MongoCrudService(MongoRepository<T, I> repository) {
    super(repository);
  }

  @Override
  public Page<T> findAll(Pageable pageable) {
    return repository.findAll(pageable);
  }
}
