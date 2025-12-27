package it.portus.smartorder.ms.invservice.api.controllers.orders;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.portus.smartorder.ms.invservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.invservice.api.controller.impl.InventoriesApiDelegateImpl;
import it.portus.smartorder.ms.invservice.api.v1.openapi.InventoriesApiController;
import it.portus.smartorder.ms.invservice.business.domain.model.Inventory;
import it.portus.smartorder.ms.invservice.business.services.InventoryService;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = InventoriesApiController.class)
@Import({ControllerTestConfig.class, InventoriesApiDelegateImpl.class})
class GetInventoryByIdControllerTest {

  private static final String ENDPOINT = "/api/v1/inventories/{id}";

  @Autowired private MockMvc mockMvc;
  @MockitoBean private InventoryService inventoryService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void getInventoryById_WhenInventoryExists_ReturnsInventory() throws Exception {
    Inventory mocked = Instancio.create(Inventory.class);
    when(inventoryService.findById(mocked.getId())).thenReturn(Optional.of(mocked));

    String responseJson =
        mockMvc
            .perform(get(ENDPOINT, mocked.getId()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    EntityModel<it.portus.smartorder.ms.invservice.api.v1.openapi.model.Inventory> response =
        objectMapper.readValue(responseJson, new TypeReference<>() {});

    Assertions.assertNotNull(response.getContent());
    assertThat(response.getContent().getId()).isEqualTo(mocked.getId());

    verify(inventoryService, times(1)).findById(mocked.getId());
  }

  @Test
  void getInventoryById_WhenInventoryNotFound_ReturnsNotFound() {
    UUID id = UUID.randomUUID();
    when(inventoryService.findById(id)).thenReturn(Optional.empty());

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(get(ENDPOINT, id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").exists()));
  }

  @Test
  void getInventoryById_WhenInvalidParameterPassed_ReturnsBadRequest() {
    assertDoesNotThrow(
        () -> mockMvc.perform(get(ENDPOINT, "bad-id")).andExpect(status().isBadRequest()));
  }

  @Test
  void getInventoryById_WhenInvalidParameterPassed_ReturnsUnprocessableEntity() {
    when(inventoryService.findById(any(UUID.class)))
        .thenThrow(new IllegalArgumentException("Invalid parameter"));

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(get(ENDPOINT, UUID.randomUUID()))
                .andExpect(status().isUnprocessableEntity()));
  }

  @Test
  void getInventoryById_WhenDatabaseUnavailable_ReturnsInternalServerErrorWithDetails() {
    when(inventoryService.findById(any(UUID.class)))
        .thenThrow(new RuntimeException("DB unavailable"));

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(get(ENDPOINT, UUID.randomUUID()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").value("DB unavailable")));
  }

  @Test
  @Disabled
  void getInventoryById_WhenUnauthorized_ReturnsUnauthorized() {
    assertDoesNotThrow(
        () -> mockMvc.perform(get(ENDPOINT, "anyId")).andExpect(status().isUnauthorized()));
  }
}
