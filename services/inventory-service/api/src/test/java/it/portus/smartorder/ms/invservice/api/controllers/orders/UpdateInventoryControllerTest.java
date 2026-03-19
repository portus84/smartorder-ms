package it.portus.smartorder.ms.invservice.api.controllers.orders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import it.portus.ms.test.controller.AbstractControllerTest;
import it.portus.smartorder.ms.invservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.invservice.api.controller.impl.InventoriesApiDelegateImpl;
import it.portus.smartorder.ms.invservice.api.v1.openapi.InventoriesApiController;
import it.portus.smartorder.ms.invservice.api.v1.openapi.model.UpdateInventoryRequest;
import it.portus.smartorder.ms.invservice.business.domain.model.Inventory;
import it.portus.smartorder.ms.invservice.business.services.InventoryService;
import java.util.Optional;
import java.util.UUID;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = InventoriesApiController.class)
@Import({ControllerTestConfig.class, InventoriesApiDelegateImpl.class})
class UpdateInventoryControllerTest extends AbstractControllerTest {

  private static final String ENDPOINT = "/api/v1/inventories/{id}";

  @MockitoBean private InventoryService inventoryService;

  @Test
  @SneakyThrows
  void updateInventory_WhenValidInventoryProvided_ReturnsUpdatedInventory() {
    Inventory mocked = Instancio.create(Inventory.class);

    when(inventoryService.update(any(UUID.class), any(Inventory.class)))
        .thenReturn(Optional.of(mocked));

    String responseJson =
        put(ENDPOINT, Instancio.create(UpdateInventoryRequest.class), mocked.getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.createdDate").exists())
            .andExpect(jsonPath("$.lastModifiedDate").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

    EntityModel<it.portus.smartorder.ms.invservice.api.v1.openapi.model.Inventory> response =
        objectMapper.readValue(responseJson, new TypeReference<>() {});

    it.portus.smartorder.ms.invservice.api.v1.openapi.model.Inventory content =
        response.getContent();
    Assertions.assertNotNull(content);
    assertEquals(mocked.getId(), content.getId());
    assertTrue(response.getLink(IanaLinkRelations.SELF.value()).isPresent());

    verify(inventoryService).update(any(UUID.class), any(Inventory.class));
  }

  @Test
  @SneakyThrows
  void updateInventory_WhenInventoryDoesNotExist_ReturnsNotFound() {
    when(inventoryService.update(any(UUID.class), any(Inventory.class)))
        .thenReturn(Optional.empty());

    put(ENDPOINT, Instancio.create(UpdateInventoryRequest.class), UUID.randomUUID())
        .andExpect(errorResponse(HttpStatus.NOT_FOUND));
  }

  @ParameterizedTest(name = "{0} => BadRequest")
  @ValueSource(strings = {"{ invalid json }", "{ }"})
  @SneakyThrows
  void updateInventory_WhenInvalidRequestBody_ReturnsBadRequest(String body) {
    put(ENDPOINT, body, UUID.randomUUID()).andExpect(errorResponse(HttpStatus.BAD_REQUEST));
  }

  @Test
  @SneakyThrows
  void updateInventory_WhenValidationFails_ReturnsUnprocessableEntity() {
    String message = "Invalid data";
    when(inventoryService.update(any(UUID.class), any(Inventory.class)))
        .thenThrow(new IllegalArgumentException(message));

    put(ENDPOINT, Instancio.create(UpdateInventoryRequest.class), UUID.randomUUID())
        .andExpect(errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, message));
  }

  @Test
  @SneakyThrows
  void updateInventory_WhenDatabaseUnavailable_ReturnsInternalServerError() {
    String message = "DB unavailable";
    when(inventoryService.update(any(UUID.class), any(Inventory.class)))
        .thenThrow(new RuntimeException(message));

    put(ENDPOINT, Instancio.create(UpdateInventoryRequest.class), UUID.randomUUID())
        .andExpect(errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message));
  }
}
