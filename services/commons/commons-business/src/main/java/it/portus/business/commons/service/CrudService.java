package it.portus.business.commons.service;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CrudService<T, ID> extends Service {

  <S extends T> S save(S entity);

  <S extends T> Optional<S> update(ID id, S entity);

  <S extends T> Iterable<S> saveAll(Iterable<S> entities);

  Optional<T> findById(ID id);

  Page<T> findAll(Pageable pageable);

  void deleteById(ID id);

  void delete(T entity);
}
