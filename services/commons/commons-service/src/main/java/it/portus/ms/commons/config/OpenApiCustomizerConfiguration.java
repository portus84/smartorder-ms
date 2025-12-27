package it.portus.ms.commons.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;
import it.portus.ms.commons.http.utils.HttpRequestUtils;
import it.portus.ms.commons.swagger.processors.OpenApiSchemaProcessor;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

@AutoConfiguration
public class OpenApiCustomizerConfiguration {

  private static final String VERSIONED_PATH_REGEX = "^/api/v\\d+";

  @Bean
  @Order(1)
  @ConditionalOnBean(OpenApiSchemaProcessor.class)
  @SuppressWarnings({"unchecked"})
  public GlobalOpenApiCustomizer globalOpenApiCustomizer(OpenApiSchemaProcessor schemaProcessor) {
    return openApi ->
        Optional.ofNullable(openApi.getComponents())
            .map(Components::getSchemas)
            .ifPresent(
                originalSchemas -> {
                  schemaProcessor.processSchemas(
                      openApi, (Map<String, Schema<?>>) (Map<?, ?>) originalSchemas);

                  rewritePathsCustomizer(openApi);
                });
  }

  private void rewritePathsCustomizer(OpenAPI openApi) {
    HttpRequestUtils.getForwardedPrefixIfFiltered()
        .ifPresent(
            forwardedPrefix -> {
              Paths originalPaths = openApi.getPaths();
              Paths newPaths = new Paths();

              originalPaths.forEach(
                  (path, item) -> {
                    String newPath = path.replaceFirst(VERSIONED_PATH_REGEX, forwardedPrefix);

                    newPaths.addPathItem(StringUtils.defaultIfBlank(newPath, "/"), item);
                  });

              openApi.setPaths(newPaths);
            });
  }
}
