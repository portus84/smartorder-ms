package it.portus.business.commons.service.utils;

import it.portus.business.commons.model.Entity;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.experimental.UtilityClass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@UtilityClass
public class ServiceUtils {

  public static <S extends Entity<?>> String[] getImmutableFields(S entity) {
    Set<String> ignored = new HashSet<>();

    ignored.add("id");

    ignored.addAll(
        Arrays.stream(entity.getClass().getDeclaredFields())
            .filter(ServiceUtils::isAuditingField)
            .map(Field::getName)
            .toList());

    return ignored.toArray(new String[0]);
  }

  private static boolean isAuditingField(Field field) {
    return field.isAnnotationPresent(CreatedDate.class)
        || field.isAnnotationPresent(LastModifiedDate.class);
  }
}
