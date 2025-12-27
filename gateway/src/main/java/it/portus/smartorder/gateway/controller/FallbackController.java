package it.portus.smartorder.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class FallbackController {

  @RequestMapping("/fallback")
  public ResponseEntity<String> fallback() {
    throw new ResponseStatusException(
        HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable. Please try again later.");
  }
}
