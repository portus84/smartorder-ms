package it.portus.ms.commons.hateoas;

import lombok.experimental.UtilityClass;
import org.springframework.hateoas.LinkRelation;

@UtilityClass
public class DomainLinkRelations {
  public static final LinkRelation DELETE = LinkRelation.of("delete");
  public static final LinkRelation PATCH = LinkRelation.of("patch");
}
