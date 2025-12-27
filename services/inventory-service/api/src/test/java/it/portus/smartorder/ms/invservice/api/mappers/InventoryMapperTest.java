package it.portus.smartorder.ms.invservice.api.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import it.portus.smartorder.ms.invservice.api.config.MappersConfig;
import it.portus.smartorder.ms.invservice.business.domain.model.Inventory;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {MappersConfig.class})
class InventoryMapperTest {

  @Autowired private InventoryMapper inventoryMapper;

  @Test
  void toDTO_ShouldMapAllFields() {
    Inventory domainBO = Instancio.create(Inventory.class);

    it.portus.smartorder.ms.invservice.api.v1.openapi.model.Inventory dto =
        inventoryMapper.toDTO(domainBO);

    assertNotNull(dto);
    assertNotNull(dto.getId());

    assertEquals(domainBO.getId(), dto.getId());
    assertEquals(domainBO.getDescription(), dto.getDescription());
  }
}
