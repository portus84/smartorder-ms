package it.portus.smartorder.ms.invservice.api.controllers.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import it.portus.ms.test.controller.AbstractControllerTest;
import it.portus.smartorder.ms.invservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.invservice.api.controller.impl.InventoriesApiDelegateImpl;
import it.portus.smartorder.ms.invservice.api.v1.openapi.InventoriesApiController;
import it.portus.smartorder.ms.invservice.api.v1.openapi.model.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = InventoriesApiController.class)
@Import({ControllerTestConfig.class, InventoriesApiDelegateImpl.class})
class PatchInventoryControllerTest extends AbstractControllerTest {

  private static final String ENDPOINT = "/api/v1/inventories/{id}";

  @MockitoBean private InventoryService inventoryService;

  @Test
  @SneakyThrows
  void patchInventory_WhenValidDetailsPatchProvided_ReturnsPatchedInventory() {
    Inventory existing = Instancio.create(Inventory.class);
    Inventory updated = Instancio.create(Inventory.class);

    when(inventoryService.findById(any(UUID.class))).thenReturn(Optional.of(existing));
    when(inventoryService.update(any(UUID.class), any(Inventory.class)))
        .thenReturn(Optional.of(updated));

    InventoryDetailsPatchOperation patchOp =
        new InventoryDetailsPatchOperation()
            .operation(InventoryPatchRequestOperation.UPDATE_DETAILS)
            .details(Instancio.create(InventoryDetailsPatchOperationDetails.class));

    verifyPatchResponse(patchOp, existing, updated);
  }

  @Test
  @SneakyThrows
  void patchInventory_WhenValidStatusPatchProvided_ReturnsPatchedInventory() {
    Inventory existing = Instancio.create(Inventory.class);
    Inventory updated = Instancio.create(Inventory.class);

    when(inventoryService.findById(any(UUID.class))).thenReturn(Optional.of(existing));
    when(inventoryService.update(any(UUID.class), any(Inventory.class)))
        .thenReturn(Optional.of(updated));

    InventoryStatusPatchOperation patchOp =
        new InventoryStatusPatchOperation()
            .operation(InventoryPatchRequestOperation.UPDATE_STATUS)
            .status(Instancio.create(InventoryStatus.class));

    verifyPatchResponse(patchOp, existing, updated);
  }

  @Test
  @SneakyThrows
  void patchInventory_WhenInventoryDoesNotExist_ReturnsNotFound() {
    when(inventoryService.findById(any(UUID.class))).thenReturn(Optional.empty());

    patch(ENDPOINT, Instancio.create(InventoryDetailsPatchOperation.class), UUID.randomUUID())
        .andExpect(errorResponse(HttpStatus.NOT_FOUND));
  }

  @ParameterizedTest(name = "{0} => BadRequest")
  @ValueSource(
      strings = {
        "{ invalid json }",
        "{ }",
        """
        {"details": { "description": "test" }}
        """,
        """
        { "operation": "DO_NOT_EXIST", "details": { "description": "test" } }
        """,
      })
  @SneakyThrows
  void patchInventory_WhenInvalidRequestBody_ReturnsBadRequest(String body) {
    patch(ENDPOINT, body, UUID.randomUUID()).andExpect(errorResponse(HttpStatus.BAD_REQUEST));
  }

  @Test
  @SneakyThrows
  void patchInventory_WhenValidationFails_ReturnsUnprocessableEntity() {
    String message = "Invalid patch data";
    Inventory existing = Instancio.create(Inventory.class);

    when(inventoryService.findById(any(UUID.class))).thenReturn(Optional.of(existing));
    when(inventoryService.update(any(UUID.class), any(Inventory.class)))
        .thenThrow(new IllegalArgumentException(message));

    patch(ENDPOINT, Instancio.create(InventoryDetailsPatchOperation.class), existing.getId())
        .andExpect(errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, message));
  }

  @Test
  @SneakyThrows
  void patchInventory_WhenDatabaseUnavailable_ReturnsInternalServerError() {
    String message = "DB unavailable";
    Inventory existing = Instancio.create(Inventory.class);

    when(inventoryService.findById(any(UUID.class))).thenReturn(Optional.of(existing));
    when(inventoryService.update(any(UUID.class), any(Inventory.class)))
        .thenThrow(new RuntimeException(message));

    patch(ENDPOINT, Instancio.create(InventoryDetailsPatchOperation.class), existing.getId())
        .andExpect(errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message));
  }

  @SneakyThrows
  private <T> void verifyPatchResponse(T patchOp, Inventory actual, Inventory expected) {
    String responseJson =
        patch(ENDPOINT, patchOp, actual.getId())
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

    assertEquals(expected.getId(), content.getId());

    verify(inventoryService).findById(any(UUID.class));
    verify(inventoryService).update(any(UUID.class), any(Inventory.class));
  }
}
