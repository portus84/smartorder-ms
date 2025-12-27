package it.portus.business.commons.model;

public interface Entity<ID> {
  ID getId();

  void setId(ID id);
}
