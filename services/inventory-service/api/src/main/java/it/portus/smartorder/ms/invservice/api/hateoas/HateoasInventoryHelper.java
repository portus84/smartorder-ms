package it.portus.smartorder.ms.invservice.api.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import it.portus.ms.commons.hateoas.utils.HATEOASLinkUtils;
import it.portus.smartorder.ms.invservice.api.v1.openapi.InventoriesApiController;
import it.portus.smartorder.ms.invservice.api.v1.openapi.model.Inventory;
import it.portus.smartorder.ms.invservice.api.v1.openapi.model.InventoryDetailsPatchOperation;
import it.portus.smartorder.ms.invservice.api.v1.openapi.model.InventoryStatusPatchOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HateoasInventoryHelper {

  private static final Class<InventoriesApiController> CONTROLLER_CLAZZ =
      InventoriesApiController.class;

  private final Environment environment;

  private final PagedResourcesAssembler<Inventory> pagedResourcesAssembler;

  public EntityModel<Inventory> toEntityModel(Inventory entity) {
    Link self = buildSelfLink(entity);

    return EntityModel.of(
        entity,
        HATEOASLinkUtils.buildLinks(
            CONTROLLER_CLAZZ,
            environment,
            self.andAffordance(
                    afford(
                        WebMvcLinkBuilder.afford(
                            methodOn(CONTROLLER_CLAZZ).deleteInventory(entity.getId()))))
                .andAffordance(
                    afford(
                        WebMvcLinkBuilder.afford(
                            methodOn(CONTROLLER_CLAZZ).updateInventory(entity.getId(), null))))
                .andAffordances(buildUpdateAffordances(self))));
  }

  public PagedModel<EntityModel<Inventory>> toPagedModel(Page<Inventory> page) {
    return pagedResourcesAssembler.toModel(page, this::toEntityModel);
  }

  private Link buildSelfLink(Inventory entity) {
    return WebMvcLinkBuilder.linkTo(methodOn(CONTROLLER_CLAZZ).getInventoryById(entity.getId()))
        .withSelfRel();
  }

  private List<Affordance> buildUpdateAffordances(Link self) {
    return Affordances.of(self)
        .afford(HttpMethod.PATCH)
        .withName("updateDetails")
        .withInput(InventoryDetailsPatchOperation.class)
        .andAfford(HttpMethod.PATCH)
        .withName("updateStatus")
        .withInput(InventoryStatusPatchOperation.class)
        .build()
        .stream()
        .map(this::afford)
        .toList();
  }

  private Affordance afford(Affordance affordance) {
    return HATEOASLinkUtils.buildAffordance(CONTROLLER_CLAZZ, environment, affordance);
  }
}
