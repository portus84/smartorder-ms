package it.portus.smartorder.ms.invservice.api;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class ApplicationTest {

  @Test
  void mainRuns() {
    try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
      mocked.when(() -> SpringApplication.run(Application.class, new String[] {})).thenReturn(null);

      Application.main(new String[] {});

      mocked.verify(() -> SpringApplication.run(Application.class, new String[] {}));
    }
  }
}
