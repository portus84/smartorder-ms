package it.portus.ms.commons.utils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@UtilityClass
public class PageableUtils {

  public static final int DEFAULT_PAGE = 0;
  public static final int DEFAULT_SIZE = 20;

  public static Pageable of(Integer page, Integer size, List<String> sort) {
    int pageNumber = Optional.ofNullable(page).orElse(DEFAULT_PAGE);
    int pageSize = Optional.ofNullable(size).orElse(DEFAULT_SIZE);

    return Optional.ofNullable(sort)
        .filter(s -> !s.isEmpty())
        .map(s -> s.stream().map(PageableUtils::parseSortString).toList())
        .map(orders -> PageRequest.of(pageNumber, pageSize, Sort.by(orders)))
        .orElseGet(() -> PageRequest.of(pageNumber, pageSize));
  }

  private static Sort.Order parseSortString(String sortStr) {
    List<String> parts =
        Stream.of(StringUtils.split(StringUtils.trimToEmpty(sortStr), ','))
            .map(StringUtils::trim)
            .toList();

    String property = parts.isEmpty() ? "" : parts.getFirst();
    Sort.Direction direction =
        parts.size() > 1 ? Sort.Direction.fromString(parts.get(1)) : Sort.Direction.ASC;

    return new Sort.Order(direction, property);
  }
}
