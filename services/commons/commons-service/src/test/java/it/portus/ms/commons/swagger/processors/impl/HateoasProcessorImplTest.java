package it.portus.ms.commons.swagger.processors.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.*;
import org.springframework.test.util.ReflectionTestUtils;

class HateoasProcessorImplTest {

  private HateoasProcessorImpl processor;

  @BeforeEach
  void setUp() {
    processor = new HateoasProcessorImpl();
  }

  @SuppressWarnings({"unchecked"})
  @Test
  void constructor_UtilitySchemasAndInternalSchemas_AddedCorrectly() {
    Set<String> utilitySchemas =
        (Set<String>) ReflectionTestUtils.getField(processor, "utilitySchemas");
    Set<String> internalSchemas =
        (Set<String>) ReflectionTestUtils.getField(processor, "internalSchemas");

    assertNotNull(utilitySchemas);
    assertTrue(utilitySchemas.contains(EntityModel.class.getSimpleName()));
    assertTrue(utilitySchemas.contains(PagedModel.class.getSimpleName()));
    assertTrue(utilitySchemas.contains(CollectionModel.class.getSimpleName()));
    assertTrue(utilitySchemas.contains(Links.class.getSimpleName()));
    assertTrue(utilitySchemas.contains(Link.class.getSimpleName()));

    assertNotNull(internalSchemas);
    assertTrue(internalSchemas.contains("Embedded"));
    assertTrue(internalSchemas.contains(PagedModel.class.getSimpleName()));
    assertTrue(internalSchemas.contains(RepresentationModel.class.getSimpleName()));
    assertTrue(internalSchemas.contains(Links.class.getSimpleName()));
    assertTrue(internalSchemas.contains(Link.class.getSimpleName()));
  }

  @Test
  void resolveSchemaFromClass_LinksClass_ReturnsObjectSchemaWithAdditionalProperties() {
    var result = processor.resolveSchemaFromClass(Links.class);
    assertTrue(result.isPresent());

    Schema<?> schema = result.get();
    assertNotNull(schema.getAdditionalProperties());
  }

  @SuppressWarnings({"rawtypes"})
  @Test
  void processSchemas_WithOriginalSchemas_SchemasProcessedAndComponentsSet() {
    OpenAPI openApi = new OpenAPI();
    Map<String, Schema<?>> originalSchemas =
        new LinkedHashMap<>(
            Map.of(
                "TestSchema", new Schema<>(),
                "Links", new Schema<>()));

    processor.processSchemas(openApi, originalSchemas);

    Components components = openApi.getComponents();
    assertNotNull(components);

    Map<String, Schema> processedSchemas = components.getSchemas();
    assertNotNull(processedSchemas);

    assertFalse(processedSchemas.containsKey("Links"));
    assertTrue(processedSchemas.containsKey("TestSchema"));
  }
}
