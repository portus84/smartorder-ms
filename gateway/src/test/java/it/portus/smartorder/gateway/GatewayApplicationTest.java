package it.portus.smartorder.gateway;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class GatewayApplicationTest {

  @Test
  void mainRuns() {
    try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
      mocked
          .when(() -> SpringApplication.run(GatewayApplication.class, new String[] {}))
          .thenReturn(null);

      GatewayApplication.main(new String[] {});

      mocked.verify(() -> SpringApplication.run(GatewayApplication.class, new String[] {}));
    }
  }
}
