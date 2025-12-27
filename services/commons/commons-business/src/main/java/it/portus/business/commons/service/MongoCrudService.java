package it.portus.business.commons.service;

import it.portus.business.commons.model.Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public abstract class MongoCrudService<T extends Entity<ID>, ID>
    extends AbstractCrudService<T, ID, MongoRepository<T, ID>> {

  public MongoCrudService(MongoRepository<T, ID> repository) {
    super(repository);
  }

  @Override
  public Page<T> findAll(Pageable pageable) {
    return repository.findAll(pageable);
  }
}
