package it.portus.ms.commons.swagger.processors;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import java.util.Set;

public interface OpenApiSchemaProcessor {

  void processSchemas(OpenAPI openApi, Map<String, Schema<?>> originalSchemas);

  void addUtilitySchemas(Set<String> schemaNames);

  void addInternalSchemas(Set<String> schemaNames);

  void addReplaceSchemas(Map<String, Schema<?>> schemaNames);
}
