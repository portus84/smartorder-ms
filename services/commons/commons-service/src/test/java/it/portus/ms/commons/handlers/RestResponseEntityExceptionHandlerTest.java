package it.portus.ms.commons.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import it.portus.ms.commons.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class RestResponseEntityExceptionHandlerTest {

  @InjectMocks private RestResponseEntityExceptionHandler exceptionHandler;

  @Test
  void handleAllExceptions_WhenRuntimeException_ReturnsInternalServerErrorResponse() {
    HttpStatus expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    RuntimeException ex = new RuntimeException();

    ErrorResponse expectedBody =
        ErrorResponse.builder()
            .errorCode(expectedStatus.name())
            .errorMessage(expectedStatus.getReasonPhrase())
            .detailMessage(ex.getLocalizedMessage())
            .build();

    ResponseEntity<Object> response =
        exceptionHandler.handleAllExceptions(ex, mock(WebRequest.class));

    assertNotNull(response);
    assertEquals(expectedBody, response.getBody());
  }

  @Test
  void handleIllegalArgumentException_WhenThrown_ReturnsUnprocessableEntityResponse() {
    HttpStatus expectedStatus = HttpStatus.UNPROCESSABLE_ENTITY;

    IllegalArgumentException ex = new IllegalArgumentException();

    ErrorResponse expectedBody =
        ErrorResponse.builder()
            .errorCode(expectedStatus.name())
            .errorMessage(expectedStatus.getReasonPhrase())
            .detailMessage(ex.getLocalizedMessage())
            .build();

    ResponseEntity<Object> response =
        exceptionHandler.handleIllegalArgumentException(ex, mock(WebRequest.class));

    assertNotNull(response);
    assertEquals(expectedBody, response.getBody());
  }

  @Test
  void handleConstraintViolationException_WhenThrown_ReturnsBadRequestResponse() {
    HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

    ConstraintViolationException ex = new ConstraintViolationException("Invalid data", null);

    ErrorResponse expectedBody =
        ErrorResponse.builder()
            .errorCode(expectedStatus.name())
            .errorMessage(expectedStatus.getReasonPhrase())
            .detailMessage(ex.getLocalizedMessage())
            .build();

    ResponseEntity<Object> response =
        exceptionHandler.handleConstraintViolationException(ex, mock(WebRequest.class));

    assertNotNull(response);
    assertEquals(expectedBody, response.getBody());
  }

  @Test
  void handleResponseStatusException_WithReason_ReturnsCorrectResponse() {
    HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;
    String reason = "Custom reason message";

    ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);

    ErrorResponse expectedBody =
        ErrorResponse.builder()
            .errorCode(expectedStatus.name())
            .errorMessage(expectedStatus.getReasonPhrase())
            .detailMessage(reason)
            .build();

    ResponseEntity<Object> response =
        exceptionHandler.handleResponseStatusException(ex, mock(WebRequest.class));

    assertNotNull(response);
    assertEquals(expectedBody, response.getBody());
  }

  @Test
  void handleMethodArgumentNotValid_WhenValidationFails_ReturnsNotFoundResponse() {
    HttpStatus expectedStatus = HttpStatus.NOT_FOUND;

    MethodArgumentNotValidException ex =
        new MethodArgumentNotValidException(mock(MethodParameter.class), mock(BindingResult.class));

    ErrorResponse expectedBody =
        ErrorResponse.builder()
            .errorCode(expectedStatus.name())
            .errorMessage(expectedStatus.getReasonPhrase())
            .detailMessage(ex.getBody().getDetail())
            .build();

    ResponseEntity<Object> response =
        exceptionHandler.handleMethodArgumentNotValid(
            ex, new HttpHeaders(), expectedStatus, mock(WebRequest.class));

    assertNotNull(response);
    assertEquals(expectedBody, response.getBody());
  }

  @Test
  void handleMethodArgumentNotValid_WhenValidationFails_ReturnsBadRequestResponse() {
    HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

    MethodArgumentNotValidException ex =
        new MethodArgumentNotValidException(mock(MethodParameter.class), mock(BindingResult.class));

    ErrorResponse expectedBody =
        ErrorResponse.builder()
            .errorCode(expectedStatus.name())
            .errorMessage(expectedStatus.getReasonPhrase())
            .detailMessage(ex.getBody().getDetail())
            .build();

    ResponseEntity<Object> response =
        exceptionHandler.handleMethodArgumentNotValid(
            ex, new HttpHeaders(), expectedStatus, mock(WebRequest.class));

    assertNotNull(response);
    assertEquals(expectedBody, response.getBody());
    assertEquals(expectedStatus, response.getStatusCode());
  }

  @Test
  void handleHttpMessageNotReadable_WhenBodyIsInvalid_ReturnsNotFoundResponse() {
    HttpStatus expectedStatus = HttpStatus.NOT_FOUND;

    HttpMessageNotReadableException ex =
        new HttpMessageNotReadableException("", mock(HttpInputMessage.class));

    ErrorResponse expectedBody =
        ErrorResponse.builder()
            .errorCode(expectedStatus.name())
            .errorMessage(expectedStatus.getReasonPhrase())
            .detailMessage("Invalid request content.")
            .build();

    ResponseEntity<Object> response =
        exceptionHandler.handleHttpMessageNotReadable(
            ex, new HttpHeaders(), expectedStatus, mock(WebRequest.class));

    assertNotNull(response);
    assertEquals(expectedBody, response.getBody());
  }

  @Test
  void handleExceptionInternal_WithCustomBody_UsesProvidedBody() {
    HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

    Exception ex = new RuntimeException("test");
    ErrorResponse customBody =
        ErrorResponse.builder()
            .errorCode("CUSTOM")
            .errorMessage("Custom error")
            .detailMessage("Custom detail")
            .build();
    WebRequest request = mock(WebRequest.class);
    ResponseEntity<Object> response =
        exceptionHandler.handleExceptionInternal(
            ex, customBody, new HttpHeaders(), expectedStatus, request);

    assertNotNull(response);
    assertEquals(customBody, response.getBody());
  }

  @Test
  void handleResponseStatusException_WhenStatusNotResolvable_ReturnsBadRequestResponse() {
    HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

    ResponseStatusException ex = new ResponseStatusException(expectedStatus, "fallback reason");

    ErrorResponse expectedBody =
        ErrorResponse.builder()
            .errorCode(expectedStatus.name())
            .errorMessage(expectedStatus.getReasonPhrase())
            .detailMessage("fallback reason")
            .build();

    ResponseEntity<Object> response =
        exceptionHandler.handleResponseStatusException(ex, mock(WebRequest.class));

    assertNotNull(response);
    assertEquals(expectedBody, response.getBody());
  }
}
