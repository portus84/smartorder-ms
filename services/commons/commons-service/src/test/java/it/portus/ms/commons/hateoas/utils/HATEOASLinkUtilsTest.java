package it.portus.ms.commons.hateoas.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootTest(
    classes = {
      HATEOASLinkUtilsTest.ControllerWithPlaceholder.class,
      HATEOASLinkUtilsTest.ControllerWithoutPlaceholder.class
    })
@TestPropertySource(properties = {"base.path=/resolved"})
class HATEOASLinkUtilsTest {

  @MockitoBean Environment environment;

  @Test
  void buildLink_ControllerHasPlaceholderAndEnvPropertyDefined_ReturnsResolvedLink() {
    when(environment.getProperty("base.path")).thenReturn("/resolved");

    Link link =
        HATEOASLinkUtils.buildLink(
            ControllerWithPlaceholder.class,
            environment,
            WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(ControllerWithPlaceholder.class).dummyMethod())
                .withSelfRel());

    assertThat(link.getHref()).endsWith("/resolved/dummy");
  }

  @Test
  void buildLink_ControllerHasPlaceholderAndEnvPropertyEmpty_ReturnsDefault() {
    when(environment.getProperty("base.path")).thenReturn("");

    Link link =
        HATEOASLinkUtils.buildLink(
            ControllerWithPlaceholder.class,
            environment,
            WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(ControllerWithPlaceholder.class).dummyMethod())
                .withSelfRel());

    assertThat(link.getHref()).endsWith("/dummy");
  }

  @Test
  void buildLink_ControllerWithoutPlaceholder_ReturnsOriginalLink() {
    when(environment.getProperty("base.path")).thenReturn("/resolved");

    Link link =
        HATEOASLinkUtils.buildLink(
            ControllerWithoutPlaceholder.class,
            environment,
            WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(ControllerWithoutPlaceholder.class).dummyMethod())
                .withSelfRel());

    assertThat(link.getHref()).endsWith("/dummy");
  }

  @Test
  void buildLinks_MultipleLinks_ResolvesAllCorrectly() {
    when(environment.getProperty("base.path")).thenReturn("/resolved");

    Link link1 =
        WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ControllerWithPlaceholder.class).dummyMethod())
            .withRel(IanaLinkRelations.SELF);

    Link link2 =
        WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ControllerWithoutPlaceholder.class).dummyMethod())
            .withRel(IanaLinkRelations.SELF);

    List<Link> links =
        HATEOASLinkUtils.buildLinks(ControllerWithPlaceholder.class, environment, link1, link2);

    assertThat(links).hasSize(2);
    assertThat(links.get(0).getHref()).endsWith("/resolved/dummy");
    assertThat(links.get(1).getHref()).endsWith("/dummy");
  }

  @Test
  void buildAffordance_ControllerHasPlaceholder_ResolvesAffordanceLinks() {
    when(environment.getProperty("base.path")).thenReturn("/resolved");

    Link baseLink =
        WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ControllerWithPlaceholder.class).dummyMethod())
            .withSelfRel();

    Affordance affordance =
        Affordances.of(baseLink).afford(HttpMethod.GET).build().stream().toList().getFirst();

    Affordance resolved =
        HATEOASLinkUtils.buildAffordance(ControllerWithPlaceholder.class, environment, affordance);

    resolved.forEach(link -> assertThat(link.getURI()).endsWith("/resolved/dummy"));
  }

  @RequestMapping("${base.path:/default}")
  static class ControllerWithPlaceholder {
    @RequestMapping("/dummy")
    public Object dummyMethod() {
      return null;
    }
  }

  @RequestMapping("/dummy")
  static class ControllerWithoutPlaceholder {
    @RequestMapping("/dummy")
    public Object dummyMethod() {
      return null;
    }
  }
}
