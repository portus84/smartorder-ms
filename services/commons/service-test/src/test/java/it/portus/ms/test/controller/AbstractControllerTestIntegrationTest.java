package it.portus.ms.test.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.portus.ms.commons.handlers.RestResponseEntityExceptionHandler;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

class AbstractControllerTestIntegrationTest {

  @RestController
  @RequestMapping("/test")
  static class DummyController {

    @GetMapping
    public Map<String, String> get() {
      return Map.of("result", "ok");
    }

    @GetMapping("/{id}")
    public Map<String, String> getById(@PathVariable("id") Integer id) {
      if (id == -1) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not Found");
      }

      return Map.of("result", "ok");
    }

    @PostMapping
    public Map<String, String> post(@RequestBody Map<String, String> body) {
      return body;
    }

    @PutMapping("/{id}")
    public Map<String, String> put(@RequestBody Map<String, String> body) {
      return body;
    }

    @PatchMapping
    public Map<String, String> patch(@RequestBody Map<String, String> body) {
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
    MockMvc mockMvc =
        MockMvcBuilders.standaloneSetup(new DummyController())
            .setControllerAdvice(new RestResponseEntityExceptionHandler())
            .build();

    controller = new TestController();

    ReflectionTestUtils.setField(controller, "mockMvc", mockMvc);
    ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
  }

  @Test
  void get_ValidEndpoint_ExpectedOk() throws Exception {
    controller.get("/test").andExpect(status().isOk());
  }

  @Test
  void getWithUriVariables_ValidEndpoint_ExpectedOk() throws Exception {
    controller.get("/test/{id}", 1).andExpect(status().isOk());
  }

  @Test
  void post_ValidBody_ExpectedOk() throws Exception {
    controller.post("/test", Map.of("a", "b")).andExpect(status().isOk());
  }

  @Test
  void put_ValidBodyWithUriVariables_ExpectedOk() throws Exception {
    controller.put("/test/{id}", Map.of("a", "b"), 1).andExpect(status().isOk());
  }

  @Test
  void patch_ValidBodyString_ExpectedOk() throws Exception {
    controller.patch("/test", Map.of("a", "b")).andExpect(status().isOk());
  }

  @Test
  void delete_ValidUriVariables_ExpectedNoContent() throws Exception {
    controller.delete("/test/{id}", "1").andExpect(status().isNoContent());
  }

  @Test
  void getWithParams_SingleValueMap_ExpectedOk() throws Exception {
    controller.getWithParams("/test", Map.of("q", "x")).andExpect(status().isOk());
  }

  @Test
  void getWithParams_MultiValueMap_ExpectedOk() throws Exception {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("q", "x");
    params.add("q", "y");

    controller.getWithParams("/test", params).andExpect(status().isOk());
  }

  @Test
  void getWithPageRequest_DefaultSort_ExpectedOk() throws Exception {
    PageRequest page = PageRequest.of(0, 10);
    controller.getWithPageRequest("/test", page).andExpect(status().isOk());
  }

  @Test
  void getWithPageRequest_WithSort_ExpectedOk() throws Exception {
    PageRequest page =
        PageRequest.of(1, 20, Sort.by(Sort.Order.asc("name"), Sort.Order.desc("id")));

    controller.getWithPageRequest("/test", page).andExpect(status().isOk());
  }

  @Test
  void errorResponse_DefaultMatcher_ExpectedAllFieldsExist() throws Exception {
    controller
        .get("/non-existing")
        .andExpect(AbstractControllerTest.errorResponse(HttpStatus.NOT_FOUND));
  }

  @Test
  void errorResponse_WithDetailMessage_ExpectedMatch() throws Exception {
    controller
        .get("/non-existing")
        .andExpect(AbstractControllerTest.errorResponse("No endpoint GET /non-existing."));
  }

  @Test
  void errorResponse_WithHttpStatusAndMessage_ExpectedMatch() throws Exception {
    controller
        .get("/test/{id}", -1)
        .andExpect(AbstractControllerTest.errorResponse(HttpStatus.NOT_FOUND, "Not Found"));
  }

  @Test
  void toJsonBody_NullBody_ExpectedIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ReflectionTestUtils.invokeMethod(controller, "toJsonBody", (Object) null));
  }

  @Test
  void toJsonBody_StringBody_ExpectedSameValue() {
    String result = ReflectionTestUtils.invokeMethod(controller, "toJsonBody", "plain-string");

    org.junit.jupiter.api.Assertions.assertEquals("plain-string", result);
  }

  static List<String> httpEndpoints() {
    return List.of("/test", "/test/{id}");
  }

  @ParameterizedTest
  @MethodSource("httpEndpoints")
  void httpMethods_AllEndpoints_ExpectedOk(String endpoint) throws Exception {
    controller.get(endpoint, 1).andExpect(status().isOk());
  }
}
