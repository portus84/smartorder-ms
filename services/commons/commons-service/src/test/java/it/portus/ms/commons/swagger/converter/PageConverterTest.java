package it.portus.ms.commons.swagger.converter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PageConverterTest {

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void testRemovePageableAndSortProperties() {
    Map<String, Schema<?>> props = new LinkedHashMap<>();
    props.put("pageable", new Schema<>());
    props.put("sort", new Schema<>());
    props.put("content", new Schema<>());
    props.put("totalElements", new Schema<>());

    Schema<?> schema = new Schema<>();
    schema.setProperties((Map) props);

    ModelConverter nextConverter = mock(ModelConverter.class);
    when(nextConverter.resolve(any(), any(), any())).thenReturn(schema);

    Iterator<ModelConverter> chain = Collections.singletonList(nextConverter).iterator();

    PageConverter converter = new PageConverter();

    AnnotatedType type = new AnnotatedType();
    type.setType(org.springframework.data.domain.Page.class);

    Schema<?> result = converter.resolve(type, mock(ModelConverterContext.class), chain);

    assertNotNull(result);
    assertNotNull(result.getProperties());
    assertFalse(result.getProperties().containsKey("pageable"));
    assertFalse(result.getProperties().containsKey("sort"));
    assertTrue(result.getProperties().containsKey("content"));
    assertTrue(result.getProperties().containsKey("totalElements"));
  }

  @Test
  void testOtherTypesAreDelegated() {
    Schema<Object> dummySchema = new Schema<>();

    ModelConverter nextConverter = mock(ModelConverter.class);
    when(nextConverter.resolve(any(), any(), any())).thenReturn(dummySchema);

    Iterator<ModelConverter> chain = Collections.singletonList(nextConverter).iterator();

    PageConverter converter = new PageConverter();
    AnnotatedType type = new AnnotatedType();
    type.setType(String.class);

    Schema<?> result = converter.resolve(type, mock(ModelConverterContext.class), chain);

    assertSame(dummySchema, result);
  }
}
