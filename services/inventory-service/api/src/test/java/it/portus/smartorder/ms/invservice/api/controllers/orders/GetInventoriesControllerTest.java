package it.portus.smartorder.ms.invservice.api.controllers.orders;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.portus.ms.commons.mappers.PageToPagedModelMapper;
import it.portus.smartorder.ms.invservice.api.config.ControllerTestConfig;
import it.portus.smartorder.ms.invservice.api.controller.impl.InventoriesApiDelegateImpl;
import it.portus.smartorder.ms.invservice.api.v1.openapi.InventoriesApiController;
import it.portus.smartorder.ms.invservice.business.domain.model.Inventory;
import it.portus.smartorder.ms.invservice.business.services.InventoryService;
import java.util.Collection;
import java.util.List;
import org.instancio.Instancio;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(controllers = InventoriesApiController.class)
@Import({ControllerTestConfig.class, InventoriesApiDelegateImpl.class})
class GetInventoriesControllerTest {

  private static final String ENDPOINT = "/api/v1/inventories";

  @Autowired private MockMvc mockMvc;
  @MockitoBean private InventoryService inventoryService;

  @Autowired private ObjectMapper objectMapper;
  @Autowired private PageToPagedModelMapper pageToPagedModelMapper;

  @Test
  void getInventories_WhenInventoriesExist_ReturnsAllInventories() throws Exception {
    List<Inventory> content = getMockedInventories();
    int contentSize = content.size();

    PageImpl<Inventory> page = new PageImpl<>(content, PageRequest.of(0, contentSize), contentSize);
    when(inventoryService.findAll(any(Pageable.class))).thenReturn(page);

    String responseJson =
        mockMvc
            .perform(get(ENDPOINT))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.inventories", hasSize(contentSize)))
            .andExpect(jsonPath("$.page.totalElements", equalTo(contentSize)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    PagedModel<EntityModel<it.portus.smartorder.ms.invservice.api.v1.openapi.model.Inventory>>
        responsePage = objectMapper.readValue(responseJson, new TypeReference<>() {});

    PagedModel<EntityModel<it.portus.smartorder.ms.invservice.api.v1.openapi.model.Inventory>>
        expectedPage =
            pageToPagedModelMapper.toPagedModel(
                page, it.portus.smartorder.ms.invservice.api.v1.openapi.model.Inventory.class);

    assertEquals(expectedPage.getMetadata(), responsePage.getMetadata());
    assertPageEquals(expectedPage, responsePage);

    verify(inventoryService, times(1)).findAll(any(Pageable.class));
  }

  @Test
  void getInventories_WhenQueryParametersProvided_ReturnsSortedPagedInventories() throws Exception {
    List<Inventory> content = getMockedInventories();
    int contentSize = content.size();

    PageRequest pageRequest =
        PageRequest.of(1, 5, Sort.by(List.of(Sort.Order.by("id").with(Sort.Direction.DESC))));

    when(inventoryService.findAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(content, pageRequest, contentSize));

    MockHttpServletRequestBuilder requestBuilder =
        get(ENDPOINT)
            .param("page", String.valueOf(pageRequest.getPageNumber()))
            .param("size", String.valueOf(pageRequest.getPageSize()));

    pageRequest
        .getSort()
        .forEach(
            o -> {
              String sortParam = o.getProperty() + "," + o.getDirection().name().toLowerCase();
              requestBuilder.param("sort", sortParam);
            });

    mockMvc
        .perform(requestBuilder)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.inventories", hasSize(contentSize)));

    verify(inventoryService, times(1)).findAll(any(Pageable.class));
  }

  @Test
  void getInventories_WhenPageNumberIsNegative_ReturnsBadRequest() {
    assertDoesNotThrow(
        () ->
            mockMvc.perform(get(ENDPOINT).param("page", "-1")).andExpect(status().isBadRequest()));
  }

  @Test
  @Disabled("Authorization not implemented yet")
  void getInventories_WhenUnauthorized_ReturnsUnauthorized() {
    assertDoesNotThrow(() -> mockMvc.perform(get(ENDPOINT)).andExpect(status().isUnauthorized()));
  }

  @Test
  void getInventories_WhenInvalidParameterPassed_ReturnsUnprocessableEntity() {
    when(inventoryService.findAll(any(Pageable.class)))
        .thenThrow(new IllegalArgumentException("Invalid parameter"));

    assertDoesNotThrow(
        () -> mockMvc.perform(get(ENDPOINT)).andExpect(status().isUnprocessableEntity()));
  }

  @Test
  void getInventories_WhenDatabaseUnavailable_ReturnsInternalServerErrorWithDetails() {
    when(inventoryService.findAll(any(Pageable.class)))
        .thenThrow(new RuntimeException("DB unavailable"));

    assertDoesNotThrow(
        () ->
            mockMvc
                .perform(get(ENDPOINT))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.detailMessage").value("DB unavailable")));
  }

  private void assertPageEquals(
      PagedModel<EntityModel<it.portus.smartorder.ms.invservice.api.v1.openapi.model.Inventory>>
          expectedPage,
      PagedModel<EntityModel<it.portus.smartorder.ms.invservice.api.v1.openapi.model.Inventory>>
          responsePage) {

    Collection<EntityModel<it.portus.smartorder.ms.invservice.api.v1.openapi.model.Inventory>>
        expected = expectedPage.getContent();
    Collection<EntityModel<it.portus.smartorder.ms.invservice.api.v1.openapi.model.Inventory>>
        actual = responsePage.getContent();

    JsonNode expectedNode = objectMapper.valueToTree(expected);
    JsonNode actualNode = objectMapper.valueToTree(actual);

    expectedNode.forEach(n -> ((ObjectNode) n).remove("links"));
    actualNode.forEach(n -> ((ObjectNode) n).remove("links"));

    assertEquals(expectedNode, actualNode);
  }

  private static List<Inventory> getMockedInventories() {
    return Instancio.ofList(Inventory.class).size(5).create();
  }
}
