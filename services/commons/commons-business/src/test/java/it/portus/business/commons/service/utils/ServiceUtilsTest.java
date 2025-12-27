package it.portus.business.commons.service.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import it.portus.business.commons.model.Entity;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

class ServiceUtilsTest {

  @Data
  @NoArgsConstructor
  private static class TestEntity implements Entity<Long> {
    private Long id;

    private String name;

    @CreatedDate private LocalDateTime customCreatedDate;

    @LastModifiedDate private LocalDateTime customLastModifiedDate;
  }

  @Test
  void getImmutableFields_WhenEntityHasAuditingFields_ReturnsIdAndAuditingFields() {
    TestEntity entity = new TestEntity();

    String[] immutableFields = ServiceUtils.getImmutableFields(entity);

    List<String> fieldsList = Arrays.asList(immutableFields);
    assertTrue(fieldsList.contains("id"));
    assertTrue(fieldsList.contains("customCreatedDate"));
    assertTrue(fieldsList.contains("customLastModifiedDate"));
  }

  @Test
  void getImmutableFields_WhenEntityHasNoAuditingFields_ReturnsOnlyId() {

    @Data
    @NoArgsConstructor
    class PlainEntity implements Entity<Long> {
      private Long id;
      private String name;
    }

    PlainEntity entity = new PlainEntity();

    String[] immutableFields = ServiceUtils.getImmutableFields(entity);

    List<String> fieldsList = Arrays.asList(immutableFields);
    assertTrue(fieldsList.contains("id"));
  }
}
