package it.portus.ms.commons.mappers;

import static org.junit.jupiter.api.Assertions.*;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

class ObjectIdMapperTest {

  private final ObjectIdMapper mapper = new ObjectIdMapper() {};

  @Test
  void objectIdToString_WithValidObjectId_ReturnsHexString() {
    ObjectId objectId = new ObjectId();
    String hex = mapper.objectIdToString(objectId);

    assertNotNull(hex);
    assertEquals(objectId.toHexString(), hex);
  }

  @Test
  void objectIdToString_WithNull_ReturnsNull() {
    assertNull(mapper.objectIdToString(null));
  }

  @Test
  void stringToObjectId_WithValidHexString_ReturnsObjectId() {
    ObjectId objectId = new ObjectId();
    String hex = objectId.toHexString();

    ObjectId result = mapper.stringToObjectId(hex);

    assertNotNull(result);
    assertEquals(objectId, result);
  }

  @Test
  void stringToObjectId_WithNullOrBlank_ReturnsNull() {
    assertNull(mapper.stringToObjectId(null));
    assertNull(mapper.stringToObjectId(""));
    assertNull(mapper.stringToObjectId("   "));
  }

  @Test
  void roundTripConversion_ObjectIdToStringAndBack_ReturnsOriginalObjectId() {
    ObjectId original = new ObjectId();
    String hex = mapper.objectIdToString(original);
    ObjectId convertedBack = mapper.stringToObjectId(hex);

    assertEquals(original, convertedBack);
  }
}
