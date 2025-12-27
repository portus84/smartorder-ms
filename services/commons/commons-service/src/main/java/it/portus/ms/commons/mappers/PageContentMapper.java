package it.portus.ms.commons.mappers;

public interface PageContentMapper<S, T> {
  T toDTO(S source);
}
