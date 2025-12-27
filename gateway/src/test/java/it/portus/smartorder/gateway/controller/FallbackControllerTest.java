package it.portus.smartorder.gateway.controller;

import it.portus.smartorder.gateway.config.ControllerTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(FallbackController.class)
@Import({ControllerTestConfig.class})
class FallbackControllerTest {

  @Autowired private WebTestClient webTestClient;

  @Test
  void fallback_WhenInvoked_ReturnsServiceUnavailableResponse() {
    webTestClient
        .get()
        .uri("/fallback")
        .header("X-Request-ID", "request-id-test")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
        .expectBody()
        .jsonPath("$.errorCode")
        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.name())
        .jsonPath("$.errorMessage")
        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
        .jsonPath("$.requestId")
        .exists()
        .jsonPath("$.detailMessage")
        .isEqualTo("Service temporarily unavailable. Please try again later.");
  }
}
