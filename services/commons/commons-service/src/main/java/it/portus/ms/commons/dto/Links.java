package it.portus.ms.commons.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Data;

@Data
@Schema(
    description = "HATEOAS Links",
    additionalProperties = Schema.AdditionalPropertiesValue.TRUE,
    example =
        """
                {
                  "self": {
                    "href": "https://api.example.com/resources/id",
                    "hreflang": "en-US",
                    "title": "Detailed Resource",
                    "type": "application/hal+json",
                    "deprecation": "https://api.example.com/api/v1/resources",
                    "profile": "https://example.com/resources/resource-detail",
                    "name": "resource",
                    "templated": false
                  }
                }
                """)
public class Links {

  private Map<String, Link> links;
}
