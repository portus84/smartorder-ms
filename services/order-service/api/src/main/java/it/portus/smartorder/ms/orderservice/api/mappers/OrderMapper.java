package it.portus.smartorder.ms.orderservice.api.mappers;

import it.portus.ms.commons.mappers.ObjectIdMapper;
import it.portus.ms.commons.mappers.PageContentMapper;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {ObjectIdMapper.class, OrderStateMapper.class})
public interface OrderMapper
    extends PageContentMapper<
        Order, it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order> {

  @Override
  @InheritConfiguration
  it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order toDTO(Order order);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "lastModifiedDate", ignore = true)
  Order toBO(it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order source);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "lastModifiedDate", ignore = true)
  Order toBO(it.portus.smartorder.ms.orderservice.api.v1.openapi.model.CreateOrderRequest source);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "lastModifiedDate", ignore = true)
  Order toBO(it.portus.smartorder.ms.orderservice.api.v1.openapi.model.UpdateOrderRequest source);
}
