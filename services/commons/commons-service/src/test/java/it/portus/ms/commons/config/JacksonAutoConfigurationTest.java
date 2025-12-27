package it.portus.ms.commons.config;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class JacksonAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(
              AutoConfigurations.of(
                  JacksonAutoConfiguration.class,
                  org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class));

  @Test
  void deserializePageWithContentOnly() {
    contextRunner.run(
        context -> {
          ObjectMapper mapper = context.getBean(ObjectMapper.class);

          List<String> contentItems = buildContentJson();

          String json = buildPageJson(contentItems, null, null, null);

          Page<ItemRecord> page = mapper.readValue(json, new TypeReference<>() {});
          assertNotNull(page);

          List<ItemRecord> content = page.getContent();
          assertNotNull(content);
          assertEquals(contentItems.size(), content.size());
          assertEquals(contentItems.size(), page.getTotalElements());

          Pageable pageable = page.getPageable();
          assertNotNull(pageable);
          assertTrue(pageable.isUnpaged());

          IntStream.range(0, content.size())
              .forEach(
                  idx -> {
                    try {
                      JsonNode expectedNode = mapper.readTree(contentItems.get(idx));
                      JsonNode actualNode =
                          mapper.readTree(mapper.writeValueAsString(content.get(idx)));
                      assertEquals(
                          expectedNode,
                          actualNode,
                          "ItemRecord at index " + idx + " does not match");
                    } catch (Exception e) {
                      fail(e);
                    }
                  });
        });
  }

  @Test
  void deserializePageWithContentAndPageable() {
    contextRunner.run(
        context -> {
          ObjectMapper mapper = context.getBean(ObjectMapper.class);

          List<String> contentItems = buildContentJson();
          int pageNumber = 1;
          int pageSize = 5;

          String json = buildPageJson(contentItems, pageNumber, pageSize, null);

          Page<ItemRecord> page = mapper.readValue(json, new TypeReference<>() {});
          assertNotNull(page);

          List<ItemRecord> content = page.getContent();
          assertNotNull(content);
          assertEquals(contentItems.size(), content.size());

          Pageable pageable = page.getPageable();
          assertNotNull(pageable);
          assertFalse(pageable.isUnpaged());
          assertEquals(pageNumber, pageable.getPageNumber());
          assertEquals(pageSize, pageable.getPageSize());

          int expectedTotal = pageable.getPageSize() + content.size();
          assertEquals(expectedTotal, page.getTotalElements());
        });
  }

  @Test
  void deserializePageWithAllAttributes() {
    contextRunner.run(
        context -> {
          ObjectMapper mapper = context.getBean(ObjectMapper.class);

          List<String> contentItems = buildContentJson();
          int pageNumber = 1;
          int pageSize = 5;
          int totalElements = 10;

          String json = buildPageJson(contentItems, pageNumber, pageSize, totalElements);

          Page<ItemRecord> page = mapper.readValue(json, new TypeReference<>() {});
          assertNotNull(page);

          List<ItemRecord> content = page.getContent();
          assertNotNull(content);
          assertEquals(contentItems.size(), content.size());
          assertEquals(totalElements, page.getTotalElements());

          Pageable pageable = page.getPageable();
          assertNotNull(pageable);
          assertFalse(pageable.isUnpaged());
          assertEquals(pageNumber, pageable.getPageNumber());
          assertEquals(pageSize, pageable.getPageSize());
        });
  }

  private static List<String> buildContentJson() {
    return IntStream.range(0, 2).mapToObj(i -> "{\"id\": \"%d\"}".formatted(i + 1)).toList();
  }

  private static String buildPageJson(
      List<String> content, Integer pageNumber, Integer pageSize, Integer totalElements) {
    List<String> fields = new LinkedList<>();

    fields.add("\"content\": [%s]".formatted(StringUtils.join(content, ", ")));

    if (pageNumber != null || pageSize != null) {
      fields.add(buildPageableJson(pageNumber, pageSize));
    }

    Optional.ofNullable(totalElements)
        .ifPresent(v -> fields.add("\"totalElements\": %d".formatted(v)));

    return "{ " + StringUtils.join(fields, ", ") + " }";
  }

  private static String buildPageableJson(Integer pageNumber, Integer pageSize) {
    List<String> pageable = new LinkedList<>();
    Optional.ofNullable(pageNumber).ifPresent(v -> pageable.add("\"pageNumber\": %d".formatted(v)));
    Optional.ofNullable(pageSize).ifPresent(v -> pageable.add("\"pageSize\": %d".formatted(v)));

    return "\"pageable\": {%s}".formatted(StringUtils.join(pageable, ", "));
  }

  public record ItemRecord(String id) {}
}
