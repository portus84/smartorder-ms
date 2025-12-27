package it.portus.smartorder.ms.orderservice.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class OrderNotFoundException extends ResponseStatusException {

  private static final String NOT_FOUND_MESSAGE = "Order with ID '%s' not found";

  public OrderNotFoundException(String id) {
    super(HttpStatus.NOT_FOUND, String.format(NOT_FOUND_MESSAGE, id));
  }
}
