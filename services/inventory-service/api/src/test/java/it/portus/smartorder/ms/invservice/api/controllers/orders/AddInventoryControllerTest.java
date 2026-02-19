package it.portus.smartorder.ms.invservice.api.controllers.orders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.portus.smartorder.ms.invservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.invservice.api.controller.impl.InventoriesApiDelegateImpl;
import it.portus.smartorder.ms.invservice.api.v1.openapi.InventoriesApiController;
import it.portus.smartorder.ms.invservice.api.v1.openapi.model.CreateInventoryRequest;
import it.portus.smartorder.ms.invservice.business.domain.model.Inventory;
import it.portus.smartorder.ms.invservice.business.services.InventoryService;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = InventoriesApiController.class)
@Import({ControllerTestConfig.class, InventoriesApiDelegateImpl.class})
class AddInventoryControllerTest {

  private static final String ENDPOINT = "/api/v1/inventories";

  @Autowired private MockMvc mockMvc;
  @MockitoBean private InventoryService inventoryService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void addInventory_WhenValidInventoryProvided_ReturnsCreatedInventory() throws Exception {
    Inventory mocked = Instancio.create(Inventory.class);

    when(inventoryService.save(any(Inventory.class))).thenReturn(mocked);

    String requestJson = objectMapper.writeValueAsString(buildCreateInventoryRequest());

    String responseJson =
        mockMvc
            .perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(requestJson))
            .andExpect(status().isCreated())
            .andExpect(header().exists(HttpHeaders.LOCATION))
            .andExpect(
                header()
                    .string(
                        HttpHeaders.LOCATION,
                        org.hamcrest.Matchers.containsString(mocked.getId().toString())))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.createdDate").exists())
            .andExpect(jsonPath("$.lastModifiedDate").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

    EntityModel<it.portus.smartorder.ms.invservice.api.v1.openapi.model.Inventory> response =
        objectMapper.readValue(responseJson, new TypeReference<>() {});

    Assertions.assertNotNull(response.getContent());
    assertEquals(mocked.getId(), response.getContent().getId());
    assertTrue(response.getLink(IanaLinkRelations.SELF.value()).isPresent());

    verify(inventoryService, times(1)).save(any(Inventory.class));
  }

  @Test
  void addInventory_WhenInvalidRequestBody_ReturnsBadRequest() {
    String invalidRequestJson = "{ invalid json }";

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest()));
  }

  @Test
  void addInventory_WhenRequiredFieldMissing_ReturnsBadRequest() {
    String requestWithMissingField = "{}";

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(
                    post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestWithMissingField))
                .andExpect(status().isBadRequest()));
  }

  @Test
  void addInventory_WhenInvalidParameterPassed_ReturnsUnprocessableEntity() {
    when(inventoryService.save(any(Inventory.class)))
        .thenThrow(new IllegalArgumentException("Invalid data"));

    CreateInventoryRequest createRequest = buildCreateInventoryRequest();

    assertDoesNotThrow(
        () -> {
          String requestJson = objectMapper.writeValueAsString(createRequest);
          mockMvc
              .perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(requestJson))
              .andExpect(status().isUnprocessableEntity());
        });

    verify(inventoryService, times(1)).save(any(Inventory.class));
  }

  @Test
  void addInventory_WhenDatabaseUnavailable_ReturnsInternalServerErrorWithDetails() {
    when(inventoryService.save(any(Inventory.class)))
        .thenThrow(new RuntimeException("DB unavailable"));

    CreateInventoryRequest createRequest = buildCreateInventoryRequest();

    assertDoesNotThrow(
        () -> {
          String requestJson = objectMapper.writeValueAsString(createRequest);
          mockMvc
              .perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(requestJson))
              .andExpect(status().isInternalServerError())
              .andExpect(jsonPath("$.errorCode").exists())
              .andExpect(jsonPath("$.errorMessage").exists())
              .andExpect(jsonPath("$.detailMessage").value("DB unavailable"));
        });

    verify(inventoryService, times(1)).save(any(Inventory.class));
  }

  @Test
  @Disabled("Authorization not implemented yet")
  void addInventory_WhenUnauthorized_ReturnsUnauthorized() {
    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized()));
  }

  private CreateInventoryRequest buildCreateInventoryRequest() {
    return Instancio.create(CreateInventoryRequest.class);
  }
}
