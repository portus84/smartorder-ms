package it.portus.ms.test.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.portus.ms.commons.handlers.RestResponseEntityExceptionHandler;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

class AbstractControllerTestIntegrationTest {

  @RestController
  @RequestMapping("/test")
  static class DummyController {

    @GetMapping
    public Map<String, String> get() {
      return Map.of("result", "ok");
    }

    @PostMapping
    public Map<String, String> post(@RequestBody Map<String, String> body) {
      return body;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {
      // test purpose
    }
  }

  static class TestController extends AbstractControllerTest {}

  private TestController controller;

  @BeforeEach
  void setup() {
    var mockMvc =
        MockMvcBuilders.standaloneSetup(new DummyController())
            .setControllerAdvice(new RestResponseEntityExceptionHandler())
            .build();

    controller = new TestController();

    ReflectionTestUtils.setField(controller, "mockMvc", mockMvc);
    ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
  }

  @Test
  void testGet() throws Exception {
    controller.get("/test").andExpect(status().isOk());
  }

  @Test
  void testPost() throws Exception {
    controller.post("/test", Map.of("a", "b")).andExpect(status().isOk());
  }

  @Test
  void testGetWithParams() throws Exception {
    controller.getWithParams("/test", Map.of("q", "x")).andExpect(status().isOk());
  }

  @Test
  void testGetWithPageRequest() throws Exception {
    PageRequest page = PageRequest.of(0, 10);
    controller.getWithPageRequest("/test", page).andExpect(status().isOk());
  }

  @Test
  void testDelete() throws Exception {
    controller.delete("/test/{id}", "1").andExpect(status().isNoContent());
  }

  @Test
  void testErrorResponseMatcher() throws Exception {
    controller
        .get("/non-existing")
        .andExpect(AbstractControllerTest.errorResponse(HttpStatus.NOT_FOUND));
  }
}
