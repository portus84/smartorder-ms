package it.portus.business.commons.service;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CrudService<T, I> extends Service {

  <S extends T> S save(S entity);

  <S extends T> Optional<S> update(I id, S entity);

  <S extends T> Iterable<S> saveAll(Iterable<S> entities);

  Optional<T> findById(I id);

  Page<T> findAll(Pageable pageable);

  void deleteById(I id);

  void delete(T entity);
}
