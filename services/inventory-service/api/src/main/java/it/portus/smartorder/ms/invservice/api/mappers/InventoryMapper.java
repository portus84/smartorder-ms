package it.portus.smartorder.ms.invservice.api.mappers;

import it.portus.ms.commons.mappers.PageContentMapper;
import it.portus.smartorder.ms.invservice.business.domain.model.Inventory;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InventoryMapper
    extends PageContentMapper<
        Inventory, it.portus.smartorder.ms.invservice.api.v1.openapi.model.Inventory> {

  @Override
  @InheritConfiguration
  it.portus.smartorder.ms.invservice.api.v1.openapi.model.Inventory toDTO(Inventory source);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "lastModifiedDate", ignore = true)
  Inventory toBO(it.portus.smartorder.ms.invservice.api.v1.openapi.model.CreateInventoryRequest source);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "lastModifiedDate", ignore = true)
  Inventory toBO(it.portus.smartorder.ms.invservice.api.v1.openapi.model.UpdateInventoryRequest source);
}
