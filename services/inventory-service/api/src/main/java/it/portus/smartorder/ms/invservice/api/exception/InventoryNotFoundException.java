package it.portus.smartorder.ms.invservice.api.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InventoryNotFoundException extends ResponseStatusException {

  private static final String NOT_FOUND_MESSAGE = "Inventory with ID '%s' not found";

  public InventoryNotFoundException(UUID id) {
    super(HttpStatus.NOT_FOUND, String.format(NOT_FOUND_MESSAGE, id));
  }
}
