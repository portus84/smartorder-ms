package it.portus.smartorder.ms.invservice.api.controllers.orders;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.portus.smartorder.ms.invservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.invservice.api.controller.impl.InventoriesApiDelegateImpl;
import it.portus.smartorder.ms.invservice.api.v1.openapi.InventoriesApiController;
import it.portus.smartorder.ms.invservice.api.v1.openapi.model.UpdateInventoryRequest;
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
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = InventoriesApiController.class)
@Import({ControllerTestConfig.class, InventoriesApiDelegateImpl.class})
class UpdateInventoryControllerTest {

  private static final String ENDPOINT = "/api/v1/inventories";

  @Autowired private MockMvc mockMvc;
  @MockitoBean private InventoryService inventoryService;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void updateInventory_WhenValidInventoryProvided_ReturnsUpdatedInventory() throws Exception {
    Inventory mocked = Instancio.create(Inventory.class);

    when(inventoryService.update(any(UUID.class), any(Inventory.class)))
        .thenReturn(Optional.of(mocked));

    String requestJson = objectMapper.writeValueAsString(buildUpdateInventoryRequest());

    String responseJson =
        mockMvc
            .perform(
                put(ENDPOINT + "/" + mocked.getId())
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
    assertThat(content.getId()).isEqualTo(mocked.getId());
    assertThat(response.getLink(IanaLinkRelations.SELF.value())).isPresent();

    verify(inventoryService, times(1)).update(any(UUID.class), any(Inventory.class));
  }

  @Test
  void updateInventory_WhenInventoryDoesNotExist_ReturnsNotFound() throws Exception {
    UUID randomId = UUID.randomUUID();

    when(inventoryService.update(any(), any())).thenReturn(Optional.empty());

    String requestJson = objectMapper.writeValueAsString(buildUpdateInventoryRequest());

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    put(ENDPOINT + "/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").exists()));

    verify(inventoryService, times(1)).update(any(UUID.class), any(Inventory.class));
  }

  @Test
  void updateInventory_WhenInvalidRequestBody_ReturnsBadRequest() {
    String invalidRequestJson = "{ invalid json }";
    UUID randomId = UUID.randomUUID();

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    put(ENDPOINT + "/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest()));
  }

  @Test
  void updateInventory_WhenValidationFails_ReturnsUnprocessableEntity() throws Exception {
    UUID randomId = UUID.randomUUID();

    when(inventoryService.update(any(UUID.class), any(Inventory.class)))
        .thenThrow(new IllegalArgumentException("Invalid data"));

    String requestJson = objectMapper.writeValueAsString(buildUpdateInventoryRequest());

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    put(ENDPOINT + "/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnprocessableEntity()));

    verify(inventoryService, times(1)).update(any(UUID.class), any(Inventory.class));
  }

  @Test
  void updateInventory_WhenDatabaseUnavailable_ReturnsInternalServerError() throws Exception {
    UUID randomId = UUID.randomUUID();

    when(inventoryService.update(any(UUID.class), any(Inventory.class)))
        .thenThrow(new RuntimeException("DB unavailable"));

    String requestJson = objectMapper.writeValueAsString(buildUpdateInventoryRequest());

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    put(ENDPOINT + "/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").value("DB unavailable")));

    verify(inventoryService, times(1)).update(any(UUID.class), any(Inventory.class));
  }

  private UpdateInventoryRequest buildUpdateInventoryRequest() {
    return Instancio.create(UpdateInventoryRequest.class);
  }
}
