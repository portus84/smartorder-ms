package it.portus.ms.test.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;

/**
 * Base class for controller tests. Provides MockMvc helpers and JSON serialization. No tests are
 * defined here because it is abstract.
 */
@SuppressWarnings("java:S6813") // Field injection is acceptable in Spring MVC tests
public abstract class AbstractControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired protected ObjectMapper objectMapper;

  @SneakyThrows
  protected <T> ResultActions post(String endpoint, T body, Object... uriVariables) {
    return mockMvc.perform(
        MockMvcRequestBuilders.post(endpoint, uriVariables)
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJsonBody(body)));
  }

  @SneakyThrows
  protected ResultActions getWithParams(String endpoint, Map<String, String> params) {
    return getWithParams(endpoint, MultiValueMap.fromSingleValue(params));
  }

  @SneakyThrows
  protected ResultActions getWithParams(String endpoint, MultiValueMap<String, String> params) {
    MockHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.get(endpoint).contentType(MediaType.APPLICATION_JSON);

    params.forEach((key, values) -> values.forEach(v -> builder.param(key, v)));

    return mockMvc.perform(builder);
  }

  @SneakyThrows
  protected ResultActions getWithPageRequest(String endpoint, PageRequest pageRequest) {
    return getWithParams(
        endpoint,
        MultiValueMap.fromMultiValue(
            Map.of(
                "page", List.of(String.valueOf(pageRequest.getPageNumber())),
                "size", List.of(String.valueOf(pageRequest.getPageSize())),
                "sort",
                    pageRequest.getSort().stream()
                        .map(o -> o.getProperty() + "," + o.getDirection().name().toLowerCase())
                        .toList())));
  }

  @SneakyThrows
  protected ResultActions get(String endpoint, Object... uriVariables) {
    return mockMvc.perform(
        MockMvcRequestBuilders.get(endpoint, uriVariables).contentType(MediaType.APPLICATION_JSON));
  }

  @SneakyThrows
  protected <T> ResultActions put(String endpoint, T body, Object... uriVariables) {
    return mockMvc.perform(
        MockMvcRequestBuilders.put(endpoint, uriVariables)
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJsonBody(body)));
  }

  @SneakyThrows
  protected <T> ResultActions patch(String endpoint, T body, Object... uriVariables) {
    return mockMvc.perform(
        MockMvcRequestBuilders.patch(endpoint, uriVariables)
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJsonBody(body)));
  }

  @SneakyThrows
  protected ResultActions delete(String endpoint, Object... uriVariables) {
    return mockMvc.perform(
        MockMvcRequestBuilders.delete(endpoint, uriVariables)
            .contentType(MediaType.APPLICATION_JSON));
  }

  public static ResultMatcher errorResponse() {
    return result -> {
      jsonPath("$.errorCode").exists().match(result);
      jsonPath("$.errorMessage").exists().match(result);
      jsonPath("$.detailMessage").exists().match(result);
    };
  }

  public static ResultMatcher errorResponse(String detailMessage) {
    return result -> {
      jsonPath("$.errorCode").exists().match(result);
      jsonPath("$.errorMessage").exists().match(result);
      jsonPath("$.detailMessage").value(detailMessage).match(result);
    };
  }

  public static ResultMatcher errorResponse(HttpStatus httpStatus) {
    return result -> {
      status().is(httpStatus.value()).match(result);
      errorResponse().match(result);
    };
  }

  public static ResultMatcher errorResponse(HttpStatus httpStatus, String detailMessage) {
    return result -> {
      status().is(httpStatus.value()).match(result);
      errorResponse(detailMessage).match(result);
    };
  }

  @SneakyThrows
  private String toJsonBody(Object obj) {
    if (obj == null) {
      throw new IllegalArgumentException("Body cannot be null");
    }

    if (obj instanceof String string) {
      return string;
    } else {
      return objectMapper.writeValueAsString(obj);
    }
  }
}
