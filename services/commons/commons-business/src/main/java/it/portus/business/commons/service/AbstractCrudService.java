package it.portus.business.commons.service;

import it.portus.business.commons.model.Entity;
import it.portus.business.commons.service.utils.ServiceUtils;
import java.util.Optional;
import org.springframework.beans.BeanUtils;
import org.springframework.data.repository.CrudRepository;

public abstract class AbstractCrudService<T extends Entity<I>, I, R extends CrudRepository<T, I>>
    implements CrudService<T, I> {

  protected final R repository;

  protected AbstractCrudService(R repository) {
    this.repository = repository;
  }

  @Override
  public <S extends T> S save(S entity) {
    return repository.save(entity);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <S extends T> Optional<S> update(I id, S entity) {
    return findById(id)
        .map(
            existing -> {
              BeanUtils.copyProperties(entity, existing, ServiceUtils.getImmutableFields(existing));
              return (S) save(existing);
            });
  }

  @Override
  public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
    return repository.saveAll(entities);
  }

  @Override
  public Optional<T> findById(I id) {
    return repository.findById(id);
  }

  @Override
  public void deleteById(I id) {
    repository.deleteById(id);
  }

  @Override
  public void delete(T entity) {
    repository.delete(entity);
  }
}
