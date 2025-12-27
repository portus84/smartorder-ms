package it.portus.ms.commons.config;

import it.portus.ms.commons.mappers.ObjectIdMapper;
import org.bson.types.ObjectId;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ObjectId.class)
public class MongoMappersConfiguration {

  @Bean
  public ObjectIdMapper objectIdMapper() {
    return Mappers.getMapper(ObjectIdMapper.class);
  }
}
