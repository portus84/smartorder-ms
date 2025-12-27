package it.portus.ms.commons.mappers;

import org.bson.types.ObjectId;
import org.mapstruct.Mapper;

@Mapper
public interface ObjectIdMapper {

  default String objectIdToString(ObjectId id) {
    return id != null ? id.toHexString() : null;
  }

  default ObjectId stringToObjectId(String id) {
    return (id != null && !id.isBlank()) ? new ObjectId(id) : null;
  }
}
