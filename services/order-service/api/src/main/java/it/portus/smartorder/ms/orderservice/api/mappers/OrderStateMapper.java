package it.portus.smartorder.ms.orderservice.api.mappers;

import it.portus.smartorder.ms.orderservice.business.domain.model.OrderState;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderStateMapper {

  it.portus.smartorder.ms.orderservice.api.v1.openapi.model.OrderState toDTO(OrderState orderState);

  OrderState toBO(it.portus.smartorder.ms.orderservice.api.v1.openapi.model.OrderState source);
}
