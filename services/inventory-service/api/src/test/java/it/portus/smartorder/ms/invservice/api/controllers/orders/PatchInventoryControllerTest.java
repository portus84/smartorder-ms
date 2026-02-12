package it.portus.smartorder.ms.invservice.api.controllers.orders;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.portus.smartorder.ms.invservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.invservice.api.controller.impl.InventoriesApiDelegateImpl;
import it.portus.smartorder.ms.invservice.api.v1.openapi.InventoriesApiController;
import it.portus.smartorder.ms.invservice.api.v1.openapi.model.*;
import it.portus.smartorder.ms.invservice.business.domain.model.Inventory;
import it.portus.smartorder.ms.invservice.business.services.InventoryService;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = InventoriesApiController.class)
@Import({ControllerTestConfig.class, InventoriesApiDelegateImpl.class})
class PatchInventoryControllerTest {

  private static final String ENDPOINT = "/api/v1/inventories";

  @Autowired private MockMvc mockMvc;
  @MockitoBean private InventoryService inventoryService;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void patchInventory_WhenValidDetailsPatchProvided_ReturnsPatchedInventory() throws Exception {
    Inventory existing = Instancio.create(Inventory.class);
    Inventory updated = Instancio.create(Inventory.class);

    when(inventoryService.findById(any(UUID.class))).thenReturn(Optional.of(existing));
    when(inventoryService.update(any(UUID.class), any(Inventory.class)))
        .thenReturn(Optional.of(updated));

    InventoryDetailsPatchOperation patchOp =
        new InventoryDetailsPatchOperation()
            .operation(InventoryPatchRequestOperation.UPDATE_DETAILS)
            .details(Instancio.create(InventoryDetailsPatchOperationDetails.class));

    String requestJson = objectMapper.writeValueAsString(patchOp);

    String responseJson =
        mockMvc
            .perform(
                patch(ENDPOINT + "/" + existing.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
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

    assertThat(content.getId()).isEqualTo(updated.getId());

    verify(inventoryService, times(1)).findById(any(UUID.class));
    verify(inventoryService, times(1)).update(any(UUID.class), any(Inventory.class));
  }

  @Test
  void patchInventory_WhenValidStatusPatchProvided_ReturnsPatchedInventory() throws Exception {
    Inventory existing = Instancio.create(Inventory.class);
    Inventory updated = Instancio.create(Inventory.class);

    when(inventoryService.findById(any(UUID.class))).thenReturn(Optional.of(existing));
    when(inventoryService.update(any(UUID.class), any(Inventory.class)))
        .thenReturn(Optional.of(updated));

    InventoryStatusPatchOperation patchOp =
        new InventoryStatusPatchOperation()
            .operation(InventoryPatchRequestOperation.UPDATE_STATUS)
            .status(Instancio.create(InventoryStatus.class));

    String requestJson = objectMapper.writeValueAsString(patchOp);

    String responseJson =
        mockMvc
            .perform(
                patch(ENDPOINT + "/" + existing.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.createdDate").exists())
            .andExpect(jsonPath("$.lastModifiedDate").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

    EntityModel<it.portus.smartorder.ms.invservice.api.v1.openapi.model.Inventory> response =
        objectMapper.readValue(responseJson, new TypeReference<>() {});

    Assertions.assertNotNull(response.getContent());
    assertThat(response.getContent().getId()).isEqualTo(updated.getId());

    verify(inventoryService, times(1)).findById(any(UUID.class));
    verify(inventoryService, times(1)).update(any(UUID.class), any(Inventory.class));
  }

  @Test
  void patchInventory_WhenInventoryDoesNotExist_ReturnsNotFound() throws Exception {
    UUID randomId = UUID.randomUUID();

    when(inventoryService.findById(any(UUID.class))).thenReturn(Optional.empty());

    InventoryDetailsPatchOperation patchOp = Instancio.create(InventoryDetailsPatchOperation.class);

    String requestJson = objectMapper.writeValueAsString(patchOp);

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    patch(ENDPOINT + "/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").exists()));

    verify(inventoryService, times(1)).findById(any(UUID.class));
  }

  @Test
  void patchInventory_WhenInvalidRequestBody_ReturnsBadRequest() {
    String invalidJson = "{ invalid json }";
    UUID randomId = UUID.randomUUID();

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    patch(ENDPOINT + "/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest()));
  }

  @Test
  void patchInventory_WhenValidationFails_ReturnsUnprocessableEntity() throws Exception {
    Inventory existing = Instancio.create(Inventory.class);

    when(inventoryService.findById(any(UUID.class))).thenReturn(Optional.of(existing));
    when(inventoryService.update(any(UUID.class), any(Inventory.class)))
        .thenThrow(new IllegalArgumentException("Invalid patch data"));

    InventoryDetailsPatchOperation patchOp = Instancio.create(InventoryDetailsPatchOperation.class);

    String requestJson = objectMapper.writeValueAsString(patchOp);

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    patch(ENDPOINT + "/" + existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnprocessableEntity()));

    verify(inventoryService, times(1)).update(any(UUID.class), any(Inventory.class));
  }

  @Test
  void patchInventory_WhenDatabaseUnavailable_ReturnsInternalServerError() throws Exception {
    Inventory existing = Instancio.create(Inventory.class);

    when(inventoryService.findById(any(UUID.class))).thenReturn(Optional.of(existing));
    when(inventoryService.update(any(UUID.class), any(Inventory.class)))
        .thenThrow(new RuntimeException("DB unavailable"));

    InventoryDetailsPatchOperation patchOp = Instancio.create(InventoryDetailsPatchOperation.class);

    String requestJson = objectMapper.writeValueAsString(patchOp);

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    patch(ENDPOINT + "/" + existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").value("DB unavailable")));

    verify(inventoryService, times(1)).update(any(UUID.class), any(Inventory.class));
  }

  @Test
  void patchInventory_WhenOperationMissing_ReturnsBadRequest() {
    UUID randomId = UUID.randomUUID();

    String jsonMissingOperation =
        """
        {
          "details": { "description": "test" }
        }
        """;

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    patch(ENDPOINT + "/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMissingOperation))
                .andExpect(status().isBadRequest()));
  }

  @Test
  void patchInventory_WhenOperationNotRecognized_ReturnsBadRequest() {
    UUID randomId = UUID.randomUUID();

    String jsonInvalidOperation =
        """
        {
          "operation": "DO_NOT_EXIST",
          "details": { "description": "test" }
        }
        """;

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    patch(ENDPOINT + "/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalidOperation))
                .andExpect(status().isBadRequest()));
  }
}
