package it.portus.smartorder.ms.invservice.api.controllers.orders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = InventoriesApiController.class)
@Import({ControllerTestConfig.class, InventoriesApiDelegateImpl.class})
class DeleteInventoryControllerTest extends AbstractControllerTest {

  private static final String ENDPOINT = "/api/v1/inventories/{id}";

  @MockitoBean private InventoryService inventoryService;

  @Test
  @SneakyThrows
  void deleteInventory_WhenInventoryExists_ReturnsNoContent() {
    UUID id = UUID.randomUUID();
    when(inventoryService.findById(id)).thenReturn(Optional.of(Instancio.create(Inventory.class)));
    doNothing().when(inventoryService).deleteById(id);

    delete(ENDPOINT, id).andExpect(status().isNoContent());
  }

  @Test
  @SneakyThrows
  void deleteInventory_WhenInventoryNotFound_ReturnsNotFound() {
    UUID id = UUID.randomUUID();
    when(inventoryService.findById(id)).thenReturn(Optional.empty());

    delete(ENDPOINT, id).andExpect(errorResponse(HttpStatus.NOT_FOUND));
  }

  @Test
  @SneakyThrows
  void deleteInventory_WhenInvalidIdPassed_ReturnsBadRequest() {
    delete(ENDPOINT, "bad-id").andExpect(errorResponse(HttpStatus.BAD_REQUEST));
  }

  @Test
  @SneakyThrows
  void deleteInventory_WhenServiceThrowsIllegalArgument_ReturnsUnprocessableEntity() {
    String message = "Invalid parameter";
    when(inventoryService.findById(any(UUID.class)))
        .thenThrow(new IllegalArgumentException(message));

    delete(ENDPOINT, UUID.randomUUID())
        .andExpect(errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, message));
  }

  @Test
  @SneakyThrows
  void deleteInventory_WhenDatabaseUnavailable_ReturnsInternalServerErrorWithDetails() {
    String message = "DB unavailable";
    when(inventoryService.findById(any(UUID.class))).thenThrow(new RuntimeException(message));

    delete(ENDPOINT, UUID.randomUUID())
        .andExpect(errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message));
  }
}
