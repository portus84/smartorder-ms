package it.portus.smartorder.ms.orderservice.api.mappers;

import static org.junit.jupiter.api.Assertions.*;

import it.portus.smartorder.ms.orderservice.api.config.MappersConfig;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {MappersConfig.class})
class OrderMapperTest {

  @Autowired private OrderMapper orderMapper;

  @Test
  void toDTO_ShouldMapAllFields() {
    Order domainBO = Instancio.create(Order.class);

    it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order dto =
        orderMapper.toDTO(domainBO);

    assertNotNull(dto);
    assertNotNull(dto.getId());

    assertDtoEqualsDomain(dto, domainBO);
  }

  private void assertDtoEqualsDomain(
      it.portus.smartorder.ms.orderservice.api.v1.openapi.model.Order dto, Order domainBO) {
    assertAll(
        () -> assertEquals(dto.getId(), domainBO.getId().toHexString()),
        () -> assertEquals(dto.getDescription(), domainBO.getDescription()),
        () -> {
          assertNotNull(dto.getState());
          assertEquals(dto.getState().getReason(), domainBO.getState().getReason());
          assertEquals(dto.getState().getStatus().name(), domainBO.getState().getStatus().name());
        },
        () -> assertNotNull(dto.getState()),
        () -> assertEquals(dto.getCreatedDate(), domainBO.getCreatedDate()),
        () -> assertEquals(dto.getLastModifiedDate(), domainBO.getLastModifiedDate()));
  }
}
