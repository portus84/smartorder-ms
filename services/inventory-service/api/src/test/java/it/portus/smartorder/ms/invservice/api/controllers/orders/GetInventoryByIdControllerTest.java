package it.portus.smartorder.ms.invservice.api.controllers.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import it.portus.ms.test.controller.AbstractControllerTest;
import it.portus.smartorder.ms.invservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.invservice.api.controller.impl.InventoriesApiDelegateImpl;
import it.portus.smartorder.ms.invservice.api.v1.openapi.InventoriesApiController;
import it.portus.smartorder.ms.invservice.business.domain.model.Inventory;
import it.portus.smartorder.ms.invservice.business.services.InventoryService;
import java.util.Optional;
import java.util.UUID;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = InventoriesApiController.class)
@Import({ControllerTestConfig.class, InventoriesApiDelegateImpl.class})
class GetInventoryByIdControllerTest extends AbstractControllerTest {

  private static final String ENDPOINT = "/api/v1/inventories/{id}";

  @MockitoBean private InventoryService inventoryService;

  @Override
  protected String endpoint() {
    return ENDPOINT;
  }

  @Test
  @SneakyThrows
  void getInventoryById_WhenInventoryExists_ReturnsInventory() {
    Inventory mocked = Instancio.create(Inventory.class);
    when(inventoryService.findById(mocked.getId())).thenReturn(Optional.of(mocked));

    String responseJson =
        get(mocked.getId())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    EntityModel<it.portus.smartorder.ms.invservice.api.v1.openapi.model.Inventory> response =
        objectMapper.readValue(responseJson, new TypeReference<>() {});

    Assertions.assertNotNull(response.getContent());
    assertEquals(mocked.getId(), response.getContent().getId());

    verify(inventoryService).findById(mocked.getId());
  }

  @Test
  @SneakyThrows
  void getInventoryById_WhenInventoryNotFound_ReturnsNotFound() {
    UUID id = UUID.randomUUID();
    when(inventoryService.findById(id)).thenReturn(Optional.empty());

    get(id).andExpect(errorResponse(HttpStatus.NOT_FOUND));
  }

  @Test
  @SneakyThrows
  void getInventoryById_WhenInvalidParameterPassed_ReturnsBadRequest() {
    get("bad-id").andExpect(errorResponse(HttpStatus.BAD_REQUEST));
  }

  @Test
  @SneakyThrows
  void getInventoryById_WhenInvalidParameterPassed_ReturnsUnprocessableEntity() {
    String message = "Invalid parameter";
    when(inventoryService.findById(any(UUID.class)))
        .thenThrow(new IllegalArgumentException(message));

    get(UUID.randomUUID()).andExpect(errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, message));
  }

  @Test
  @SneakyThrows
  void getInventoryById_WhenDatabaseUnavailable_ReturnsInternalServerErrorWithDetails() {
    String message = "DB unavailable";
    when(inventoryService.findById(any(UUID.class))).thenThrow(new RuntimeException(message));

    get(UUID.randomUUID()).andExpect(errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message));
  }

  @Test
  @SneakyThrows
  @Disabled("Authorization not implemented yet")
  void getInventoryById_WhenUnauthorized_ReturnsUnauthorized() {
    get("anyId").andExpect(status().isUnauthorized());
  }
}
