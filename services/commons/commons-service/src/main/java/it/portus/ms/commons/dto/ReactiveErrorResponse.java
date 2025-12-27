package it.portus.ms.commons.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ReactiveErrorResponse extends ErrorResponse {

  private String requestId;
}
