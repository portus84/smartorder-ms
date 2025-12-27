package it.portus.smartorder.gateway.controller;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Home redirection to api documentation */
@RestController
public class HomeController {

  @RequestMapping("/")
  public ResponseEntity<Void> index() {
    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/scalar")).build();
  }
}
