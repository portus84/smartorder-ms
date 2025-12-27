package it.portus.smartorder.ms.invservice.api.controllers.orders;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import it.portus.smartorder.ms.invservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.invservice.api.controller.impl.InventoriesApiDelegateImpl;
import it.portus.smartorder.ms.invservice.api.v1.openapi.InventoriesApiController;
import it.portus.smartorder.ms.invservice.business.domain.model.Inventory;
import it.portus.smartorder.ms.invservice.business.services.InventoryService;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = InventoriesApiController.class)
@Import({ControllerTestConfig.class, InventoriesApiDelegateImpl.class})
class DeleteInventoryControllerTest {

  private static final String ENDPOINT = "/api/v1/inventories/{id}";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private InventoryService inventoryService;

  @Test
  void deleteInventory_WhenInventoryExists_ReturnsNoContent() {
    UUID id = UUID.randomUUID();

    when(inventoryService.findById(id)).thenReturn(Optional.of(Instancio.create(Inventory.class)));
    doNothing().when(inventoryService).deleteById(id);

    assertDoesNotThrow(
        () -> mockMvc.perform(delete(ENDPOINT, id)).andExpect(status().isNoContent()));
  }

  @Test
  void deleteInventory_WhenInventoryNotFound_ReturnsNotFound() {
    UUID id = UUID.randomUUID();

    when(inventoryService.findById(id)).thenReturn(Optional.empty());

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(delete(ENDPOINT, id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").exists()));
  }

  @Test
  void deleteInventory_WhenInvalidIdPassed_ReturnsBadRequest() {
    assertDoesNotThrow(
        () -> mockMvc.perform(delete(ENDPOINT, "bad-id")).andExpect(status().isBadRequest()));
  }

  @Test
  void deleteInventory_WhenServiceThrowsIllegalArgument_ReturnsUnprocessableEntity() {
    when(inventoryService.findById(any(UUID.class)))
        .thenThrow(new IllegalArgumentException("Invalid parameter"));

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(delete(ENDPOINT, UUID.randomUUID()))
                .andExpect(status().isUnprocessableEntity()));
  }

  @Test
  void deleteInventory_WhenDatabaseUnavailable_ReturnsInternalServerErrorWithDetails() {
    when(inventoryService.findById(any(UUID.class)))
        .thenThrow(new RuntimeException("DB unavailable"));

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(delete(ENDPOINT, UUID.randomUUID()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").value("DB unavailable")));
  }
}
