package it.portus.smartorder.ms.invservice.api.controllers.orders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import it.portus.ms.test.controller.AbstractControllerTest;
import it.portus.smartorder.ms.invservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.invservice.api.controller.impl.InventoriesApiDelegateImpl;
import it.portus.smartorder.ms.invservice.api.v1.openapi.InventoriesApiController;
import it.portus.smartorder.ms.invservice.api.v1.openapi.model.CreateInventoryRequest;
import it.portus.smartorder.ms.invservice.business.domain.model.Inventory;
import it.portus.smartorder.ms.invservice.business.services.InventoryService;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = InventoriesApiController.class)
@Import({ControllerTestConfig.class, InventoriesApiDelegateImpl.class})
class AddInventoryControllerTest extends AbstractControllerTest {

  private static final String ENDPOINT = "/api/v1/inventories";

  @MockitoBean private InventoryService inventoryService;

  @Test
  @SneakyThrows
  void addInventory_WhenValidInventoryProvided_ReturnsCreatedInventory() {
    Inventory mocked = Instancio.create(Inventory.class);

    when(inventoryService.save(any(Inventory.class))).thenReturn(mocked);

    String responseJson =
        post(ENDPOINT, Instancio.create(CreateInventoryRequest.class))
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

    assertNotNull(response.getContent());
    assertEquals(mocked.getId(), response.getContent().getId());
    assertTrue(response.getLink(IanaLinkRelations.SELF.value()).isPresent());

    verify(inventoryService).save(any(Inventory.class));
  }

  @ParameterizedTest(name = "{0} => BadRequest")
  @ValueSource(strings = {"{ invalid json }", "{ }"})
  @SneakyThrows
  void addInventory_WhenInvalidRequestBody_ReturnsBadRequest(String body) {
    post(ENDPOINT, body).andExpect(errorResponse(HttpStatus.BAD_REQUEST));
  }

  @Test
  @SneakyThrows
  void addInventory_WhenInvalidParameterPassed_ReturnsUnprocessableEntity() {
    String message = "Invalid data";
    when(inventoryService.save(any(Inventory.class)))
        .thenThrow(new IllegalArgumentException(message));

    post(ENDPOINT, Instancio.create(CreateInventoryRequest.class))
        .andExpect(errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, message));
  }

  @Test
  @SneakyThrows
  void addInventory_WhenDatabaseUnavailable_ReturnsInternalServerErrorWithDetails() {
    String message = "DB unavailable";
    when(inventoryService.save(any(Inventory.class))).thenThrow(new RuntimeException(message));

    post(ENDPOINT, Instancio.create(CreateInventoryRequest.class))
        .andExpect(errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message));
  }

  @Test
  @Disabled("Authorization not implemented yet")
  @SneakyThrows
  void addInventory_WhenUnauthorized_ReturnsUnauthorized() {
    post(ENDPOINT, "{}").andExpect(status().isUnauthorized());
  }
}
