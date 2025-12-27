package it.portus.ms.commons.hateoas;

import org.atteo.evo.inflector.English;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.config.HateoasConfiguration;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass(HateoasConfiguration.class)
public class PluralizingRelProvider implements LinkRelationProvider {

  @Override
  public LinkRelation getItemResourceRelFor(Class<?> type) {
    return LinkRelation.of(type.getSimpleName().toLowerCase());
  }

  @Override
  public LinkRelation getCollectionResourceRelFor(Class<?> type) {
    return LinkRelation.of(English.plural(type.getSimpleName().toLowerCase()));
  }

  @Override
  public boolean supports(LookupContext delimiter) {
    return true;
  }
}
