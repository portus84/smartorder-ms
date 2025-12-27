package it.portus.ms.commons.handlers;

import it.portus.ms.commons.dto.ReactiveErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.Optional;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;

@ControllerAdvice
@ConditionalOnClass(ResponseEntityExceptionHandler.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ReactiveResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  public static final class HttpHeadersConstants {
    public static final String X_REQUEST_ID = "X-Request-ID";
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public Mono<ResponseEntity<Object>> handleIllegalArgumentException(
      IllegalArgumentException ex, ServerWebExchange exchange) {
    return buildResponse(ex, HttpStatus.UNPROCESSABLE_ENTITY, null, getRequestId(exchange));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public Mono<ResponseEntity<Object>> handleConstraintViolationException(
      ConstraintViolationException ex, ServerWebExchange exchange) {
    return buildResponse(ex, HttpStatus.BAD_REQUEST, null, getRequestId(exchange));
  }

  @Override
  protected Mono<ResponseEntity<Object>> handleWebExchangeBindException(
      WebExchangeBindException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      ServerWebExchange exchange) {
    return buildResponse(
        ex, HttpStatus.valueOf(status.value()), ex.getReason(), getRequestId(exchange));
  }

  @Override
  protected Mono<ResponseEntity<Object>> handleServerWebInputException(
      ServerWebInputException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      ServerWebExchange exchange) {
    return buildResponse(
        ex, HttpStatus.valueOf(status.value()), "Invalid request content.", getRequestId(exchange));
  }

  @Override
  protected Mono<ResponseEntity<Object>> handleResponseStatusException(
      ResponseStatusException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      ServerWebExchange exchange) {
    HttpStatus httpStatus =
        Optional.ofNullable(HttpStatus.resolve(status.value())).orElse(HttpStatus.BAD_REQUEST);

    return buildResponse(ex, httpStatus, ex.getReason(), getRequestId(exchange));
  }

  @Override
  protected Mono<ResponseEntity<Object>> handleErrorResponseException(
      ErrorResponseException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      ServerWebExchange exchange) {
    return buildResponse(
        ex, HttpStatus.valueOf(status.value()), ex.getBody().getDetail(), getRequestId(exchange));
  }

  @Override
  protected Mono<ResponseEntity<Object>> handleMethodValidationException(
      MethodValidationException ex, HttpStatus status, ServerWebExchange exchange) {
    return buildResponse(ex, status, "Validation failed.", getRequestId(exchange));
  }

  private Mono<ResponseEntity<Object>> buildResponse(
      Exception ex, HttpStatus status, @Nullable String detail, String requestId) {
    return Mono.just(
        ResponseEntity.status(status).body(buildErrorResponse(ex, status, detail, requestId)));
  }

  private static ReactiveErrorResponse buildErrorResponse(
      Exception ex, HttpStatus status, @Nullable String detailMessage, String requestId) {
    return ReactiveErrorResponse.builder()
        .errorCode(status.name())
        .errorMessage(status.getReasonPhrase())
        .detailMessage(
            detailMessage != null ? detailMessage : ExceptionUtils.getRootCauseMessage(ex))
        .requestId(requestId)
        .build();
  }

  private static String getRequestId(ServerWebExchange exchange) {
    return Optional.ofNullable(
            exchange.getRequest().getHeaders().getFirst(HttpHeadersConstants.X_REQUEST_ID))
        .orElse(exchange.getRequest().getId());
  }
}
