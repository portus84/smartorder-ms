package it.portus.business.commons.model;

public interface Entity<I> {
  I getId();

  void setId(I id);
}
