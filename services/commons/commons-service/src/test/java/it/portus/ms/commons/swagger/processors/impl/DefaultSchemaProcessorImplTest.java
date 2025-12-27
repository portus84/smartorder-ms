package it.portus.ms.commons.swagger.processors.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings({"unchecked"})
class DefaultSchemaProcessorImplTest {

  private DefaultSchemaProcessorImpl processor;

  @BeforeEach
  void setUp() {
    processor = new DefaultSchemaProcessorImpl();
  }

  @Test
  void addUtilitySchemas_NewSchemas_UtilitySchemasAdded() {
    Set<String> newSchemas = Set.of("CustomSchema1", "CustomSchema2");
    processor.addUtilitySchemas(newSchemas);

    Set<String> utilitySchemas =
        (Set<String>) ReflectionTestUtils.getField(processor, "utilitySchemas");
    assertNotNull(utilitySchemas);
    assertTrue(utilitySchemas.containsAll(newSchemas));
  }

  @Test
  void addInternalSchemas_NewSchemas_InternalSchemasAdded() {
    Set<String> newSchemas = Set.of("Internal1", "Internal2");
    processor.addInternalSchemas(newSchemas);

    Set<String> internalSchemas =
        (Set<String>) ReflectionTestUtils.getField(processor, "internalSchemas");
    assertNotNull(internalSchemas);
    assertTrue(internalSchemas.containsAll(newSchemas));
  }

  @Test
  void addReplaceSchemas_NewSchemas_ReplaceSchemasAdded() {
    Schema<String> schema = new Schema<>();
    Map<String, Schema<?>> newSchemas = Map.of("ReplacedSchema", schema);
    processor.addReplaceSchemas(newSchemas);

    Map<String, Schema<?>> replaceSchemas =
        (Map<String, Schema<?>>) ReflectionTestUtils.getField(processor, "replaceSchemas");
    assertNotNull(replaceSchemas);
    assertEquals(schema, replaceSchemas.get("ReplacedSchema"));
  }

  @SuppressWarnings({"rawtypes"})
  @Test
  void processSchemas_WithOriginalSchemas_SchemasProcessedAndComponentsSet() {
    OpenAPI openApi = new OpenAPI();
    Map<String, Schema<?>> originalSchemas = new HashMap<>();

    Schema<String> testSchema = new Schema<>();
    originalSchemas.put("TestSchema", testSchema);

    processor.addInternalSchemas(Set.of("InternalSchema"));
    Schema<String> internalSchema = new Schema<>();
    originalSchemas.put("InternalSchema", internalSchema);

    processor.processSchemas(openApi, originalSchemas);

    Components components = openApi.getComponents();
    assertNotNull(components);
    Map<String, Schema> processedSchemas = components.getSchemas();
    assertNotNull(processedSchemas);

    assertFalse(processedSchemas.containsKey("InternalSchema"));
    assertTrue(processedSchemas.containsKey("TestSchema"));
  }
}
