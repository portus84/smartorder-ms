package it.portus.smartorder.ms.orderservice.api.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import it.portus.smartorder.ms.orderservice.api.config.MappersConfig;
import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import org.bson.types.ObjectId;
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

    assertThat(dto)
        .usingRecursiveComparison()
        .withComparatorForFields(
            (o1, o2) -> {
              String idDTO = (String) o1;
              ObjectId idBO = (ObjectId) o2;

              return idDTO != null ? idDTO.compareTo(idBO.toHexString()) : -1;
            },
            "id")
        .comparingOnlyFields("id", "description", "state", "createdDate", "lastModifiedDate")
        .isEqualTo(domainBO);
  }
}
