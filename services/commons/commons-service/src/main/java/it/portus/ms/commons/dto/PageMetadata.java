package it.portus.ms.commons.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@Schema(name = "PageMetadata")
public class PageMetadata {

  @Schema(
      name = "size",
      description = "Amount of items on this page.",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("size")
  private @Nullable BigDecimal size;

  @Valid
  @Schema(
      name = "totalElements",
      description = "Total amount of items of the resource.",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalElements")
  private @Nullable BigDecimal totalElements;

  @Valid
  @Schema(
      name = "totalPages",
      description = "Total amount of pages for this resource.",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalPages")
  private @Nullable BigDecimal totalPages;

  @Valid
  @Schema(
      name = "number",
      description = "Current page number.",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("number")
  private @Nullable BigDecimal number;
}
