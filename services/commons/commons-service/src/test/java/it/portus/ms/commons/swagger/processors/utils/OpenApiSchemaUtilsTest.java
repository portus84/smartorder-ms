package it.portus.ms.commons.swagger.processors.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.*;
import org.junit.jupiter.api.Test;

class OpenApiSchemaUtilsTest {

  @SuppressWarnings("unused")
  static class SampleClass {
    public String name;
    public int age;
  }

  @Test
  void filterSchemas_WithMatchingPredicate_ReturnsFilteredTreeMap() {
    Map<String, Schema<?>> input = new HashMap<>();
    input.put("a", new Schema<String>().type("string"));
    input.put("b", new Schema<String>().type("integer"));

    TreeMap<String, Schema<?>> result =
        OpenApiSchemaUtils.filterSchemas(input, entry -> "a".equals(entry.getKey()));

    assertEquals(1, result.size());
    assertTrue(result.containsKey("a"));
    assertEquals("string", result.get("a").getType());
  }

  @Test
  void inlineInternalSchemas_WithRef_ReplacesRefWithResolvedSchema() {
    Map<String, Schema<?>> lookup = new HashMap<>();
    lookup.put("MySchema", new Schema<String>().type("string"));

    Schema<String> schema = new Schema<>();
    schema.setProperties(new HashMap<>());
    Schema<String> prop = new Schema<>();
    prop.set$ref("#/components/schemas/MySchema");
    schema.getProperties().put("prop", prop);

    OpenApiSchemaUtils.inlineInternalSchemas(schema, lookup);

    Schema<?> resultProp = schema.getProperties().get("prop");
    assertNotNull(resultProp);
    assertEquals("string", resultProp.getType());
    assertNotSame(prop, resultProp);
  }

  @Test
  void inlineInternalSchemas_WithArraySchema_InlinesItems() {
    Map<String, Schema<?>> lookup = new HashMap<>();
    lookup.put("Item", new Schema<String>().type("string"));

    ArraySchema arraySchema = new ArraySchema();
    Schema<String> itemRef = new Schema<>();
    itemRef.set$ref("#/components/schemas/Item");
    arraySchema.setItems(itemRef);

    OpenApiSchemaUtils.inlineInternalSchemas(arraySchema, lookup);

    Schema<?> resultItem = arraySchema.getItems();
    assertNotNull(resultItem);
    assertEquals("string", resultItem.getType());
    assertNotSame(itemRef, resultItem);
  }

  @Test
  void resolveSchemaFromClass_WithValidClass_ReturnsOptionalSchema() {
    Optional<Schema<SampleClass>> result =
        OpenApiSchemaUtils.resolveSchemaFromClass(SampleClass.class);

    assertTrue(result.isPresent());
    Schema<?> schema = result.get();
    assertNotNull(schema.getProperties());
    assertTrue(schema.getProperties().containsKey("name"));
    assertTrue(schema.getProperties().containsKey("age"));
  }

  @Test
  void resolveSchemaFromClass_WithNullClass_ReturnsEmptyOptional() {
    Optional<Schema<Object>> result = OpenApiSchemaUtils.resolveSchemaFromClass(null);

    assertTrue(result.isEmpty());
  }
}
