package it.portus.ms.commons.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class ErrorResponse {

  private String errorCode;

  private String errorMessage;

  private String detailMessage;
}
