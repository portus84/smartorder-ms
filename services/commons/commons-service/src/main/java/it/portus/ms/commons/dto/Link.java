package it.portus.ms.commons.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Schema(
    description =
        "Represents a single HATEOAS link, providing relational information and URL to another resource, compliant with the RFC 8288 Web Linking specification.")
public class Link {

  @Schema(
      description =
          "The **required** target URI of the link. This may contain URI template variables if 'templated' is true.",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String href;

  @Schema(
      description =
          "A hint indicating the **language** of the target resource, specified as a BCP47 language tag (e.g., 'en-US').",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String hreflang;

  @Schema(
      description =
          "A **human-readable title** for the link, often used to describe its purpose to consumers.",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String title;

  @Schema(
      description =
          "The **media type** (MIME type) of the resource targeted by the link (e.g., 'application/json', 'image/png').",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String type;

  @Schema(
      description =
          "A URI that points to a document describing the **deprecation** of the link's target. Used for API versioning and evolution.",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String deprecation;

  @Schema(
      description =
          "A URI that refers to a **profile** (e.g., a documentation or schema artifact) describing the target resource format.",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String profile;

  @Schema(
      description =
          "An optional **name** for the link, used to distinguish multiple links that share the same relation (rel) value.",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String name;

  @Schema(
      description =
          "A boolean indicating if the 'href' value is a **URI Template** (RFC 6570) containing variables (e.g., '/resources/{id}').",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private Boolean templated;
}
