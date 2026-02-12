package it.portus.smartorder.gateway.controller;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Home redirection to api documentation */
@RestController
public class HomeController {

  @GetMapping("/")
  public ResponseEntity<Void> index() {
    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/scalar")).build();
  }
}
