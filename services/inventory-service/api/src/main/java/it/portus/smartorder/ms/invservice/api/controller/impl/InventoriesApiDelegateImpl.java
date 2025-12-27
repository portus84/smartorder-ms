package it.portus.smartorder.ms.invservice.api.controller.impl;

import it.portus.ms.commons.mappers.PageToPageMapper;
import it.portus.ms.commons.utils.PageableUtils;
import it.portus.smartorder.ms.invservice.api.exception.InventoryNotFoundException;
import it.portus.smartorder.ms.invservice.api.hateoas.HateoasInventoryHelper;
import it.portus.smartorder.ms.invservice.api.mappers.InventoryMapper;
import it.portus.smartorder.ms.invservice.api.v1.openapi.InventoriesApiDelegate;
import it.portus.smartorder.ms.invservice.api.v1.openapi.model.*;
import it.portus.smartorder.ms.invservice.business.services.InventoryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class InventoriesApiDelegateImpl implements InventoriesApiDelegate {

  private final InventoryService inventoryService;

  private final InventoryMapper inventoryMapper;
  private final PageToPageMapper pageToPageMapper;

  private final HateoasInventoryHelper hateoasHelper;

  @Override
  public ResponseEntity<PagedModel<EntityModel<Inventory>>> getInventories(
      Integer page, Integer size, List<String> sort) {
    return ResponseEntity.ok(
        hateoasHelper.toPagedModel(
            pageToPageMapper.toPage(
                inventoryService.findAll(PageableUtils.of(page, size, sort)), Inventory.class)));
  }

  @Override
  public ResponseEntity<EntityModel<Inventory>> getInventoryById(UUID id) {
    return inventoryService
        .findById(id)
        .map(inventoryMapper::toDTO)
        .map(hateoasHelper::toEntityModel)
        .map(ResponseEntity::ok)
        .orElseThrow(() -> new InventoryNotFoundException(id));
  }

  @SneakyThrows
  @Override
  public ResponseEntity<EntityModel<Inventory>> addInventory(CreateInventoryRequest request) {
    EntityModel<Inventory> createdEntity =
        hateoasHelper.toEntityModel(
            inventoryMapper.toDTO(inventoryService.save(inventoryMapper.toBO(request))));

    return ResponseEntity.created(
            UriComponentsBuilder.fromUriString(
                    createdEntity.getRequiredLink(IanaLinkRelations.SELF).getHref())
                .build()
                .toUri())
        .body(createdEntity);
  }

  @Override
  public ResponseEntity<EntityModel<Inventory>> updateInventory(
      UUID id, UpdateInventoryRequest updateRequest) {
    return inventoryService
        .update(id, inventoryMapper.toBO(updateRequest))
        .map(updatedEntity -> hateoasHelper.toEntityModel(inventoryMapper.toDTO(updatedEntity)))
        .map(ResponseEntity::ok)
        .orElseThrow(() -> new InventoryNotFoundException(id));
  }

  @Override
  public ResponseEntity<Void> deleteInventory(UUID id) {
    return inventoryService
        .findById(id)
        .map(
            existing -> {
              inventoryService.delete(existing);
              return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
            })
        .orElseThrow(() -> new InventoryNotFoundException(id));
  }

  @Override
  public ResponseEntity<EntityModel<Inventory>> patchInventory(
      UUID id, InventoryPatchRequest patchRequest) {
    return inventoryService
        .findById(id)
        .flatMap(
            existing ->
                inventoryService.update(existing.getId(), applyPatch(existing, patchRequest)))
        .map(
            updatedEntity ->
                ResponseEntity.ok(
                    hateoasHelper.toEntityModel(inventoryMapper.toDTO(updatedEntity))))
        .orElseThrow(() -> new InventoryNotFoundException(id));
  }

  private it.portus.smartorder.ms.invservice.business.domain.model.Inventory applyPatch(
      it.portus.smartorder.ms.invservice.business.domain.model.Inventory existing,
      InventoryPatchRequest patchRequest) {
    return switch (patchRequest) {
      case InventoryDetailsPatchOperation op ->
          existing.toBuilder().description(op.getDetails().getDescription()).build();
      case InventoryStatusPatchOperation op ->
          existing.toBuilder()
              .status(
                  it.portus.smartorder.ms.invservice.business.domain.model.InventoryStatus.valueOf(
                      op.getStatus().getValue()))
              .build();
      default -> throw new IllegalStateException("Unexpected value: " + patchRequest);
    };
  }
}
