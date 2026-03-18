package it.portus.ms.commons.handlers;

import it.portus.ms.commons.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.Optional;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@ConditionalOnClass(ResponseEntityExceptionHandler.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  @Nullable
  @ExceptionHandler(IllegalArgumentException.class)
  protected ResponseEntity<Object> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {
    return handleException(ex, request, HttpStatus.UNPROCESSABLE_ENTITY, null);
  }

  @Nullable
  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<Object> handleConstraintViolationException(
      ConstraintViolationException ex, WebRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, null);
  }

  @Nullable
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  protected ResponseEntity<Object> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex, WebRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, null);
  }

  @Nullable
  @ExceptionHandler(ResponseStatusException.class)
  protected ResponseEntity<Object> handleResponseStatusException(
      ResponseStatusException ex, WebRequest request) {
    return handleException(
        ex,
        request,
        Optional.ofNullable(HttpStatus.resolve(ex.getStatusCode().value()))
            .orElse(HttpStatus.BAD_REQUEST),
        ex.getReason());
  }

  @Nullable
  @ExceptionHandler(Exception.class)
  protected ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
    return handleExceptionInternal(
        ex, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  /*
   * Spring override; return value may be null (@Nullable). Sonar S2638 is a false positive due to
   * package-level nullability mismatch.
   */
  @SuppressWarnings("java:S2638")
  @Override
  @Nullable
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    return handleValidationException(ex, headers, status, request, ex.getBody().getDetail());
  }

  /*
   * Spring override; return value may be null (@Nullable). Sonar S2638 is a false positive due to
   * package-level nullability mismatch.
   */
  @SuppressWarnings("java:S2638")
  @Override
  @Nullable
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    return handleValidationException(ex, headers, status, request, "Invalid request content.");
  }

  /*
   * Spring override; return value may be null (@Nullable). Sonar S2638 is a false positive due to
   * package-level nullability mismatch.
   */
  @SuppressWarnings("java:S2638")
  @Override
  @Nullable
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex,
      @Nullable Object body,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    return super.handleExceptionInternal(
        ex,
        Optional.ofNullable(body)
            .orElseGet(() -> buildErrorResponse(ex, HttpStatus.valueOf(status.value()), null)),
        headers,
        status,
        request);
  }

  @Nullable
  private ResponseEntity<Object> handleException(
      Exception ex, WebRequest request, HttpStatus status, @Nullable String detailMessage) {
    return handleExceptionInternal(
        ex, buildErrorResponse(ex, status, detailMessage), new HttpHeaders(), status, request);
  }

  @Nullable
  private ResponseEntity<Object> handleValidationException(
      Exception ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request,
      @Nullable String detailMessage) {
    return handleExceptionInternal(
        ex,
        buildErrorResponse(ex, HttpStatus.valueOf(status.value()), detailMessage),
        headers,
        status,
        request);
  }

  private static ErrorResponse buildErrorResponse(
      Exception ex, HttpStatus status, @Nullable String detailMessage) {
    return ErrorResponse.builder()
        .errorCode(status.name())
        .errorMessage(status.getReasonPhrase())
        .detailMessage(
            detailMessage != null
                ? detailMessage
                : ExceptionUtils.getRootCause(ex).getLocalizedMessage())
        .build();
  }
}
