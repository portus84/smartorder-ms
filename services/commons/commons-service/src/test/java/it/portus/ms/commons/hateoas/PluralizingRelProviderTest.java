package it.portus.ms.commons.hateoas;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.LinkRelation;

class PluralizingRelProviderTest {

  private final PluralizingRelProvider provider = new PluralizingRelProvider();

  static class Inventory {}

  static class Product {}

  static class Company {}

  static class Box {}

  @Test
  void getItemResourceRelFor_givenClass_returnsLowercaseSingular() {
    LinkRelation rel = provider.getItemResourceRelFor(Product.class);
    assertEquals("product", rel.value());
  }

  @Test
  void getItemResourceRelFor_givenClassEndingWithY_returnsLowercaseSingular() {
    LinkRelation rel = provider.getItemResourceRelFor(Inventory.class);
    assertEquals("inventory", rel.value());
  }

  @Test
  void getCollectionResourceRelFor_givenRegularNoun_returnsPluralWithS() {
    LinkRelation rel = provider.getCollectionResourceRelFor(Product.class);
    assertEquals("products", rel.value());
  }

  @Test
  void getCollectionResourceRelFor_givenWordEndingWithY_returnsPluralEndingWithIes() {
    LinkRelation rel = provider.getCollectionResourceRelFor(Inventory.class);
    assertEquals("inventories", rel.value());
  }

  @Test
  void getCollectionResourceRelFor_givenIrregularPlural_returnsCorrectPlural() {
    LinkRelation rel = provider.getCollectionResourceRelFor(Box.class);
    assertEquals("boxes", rel.value());
  }

  @Test
  void getCollectionResourceRelFor_givenCompany_returnsCompanies() {
    LinkRelation rel = provider.getCollectionResourceRelFor(Company.class);
    assertEquals("companies", rel.value());
  }
}
