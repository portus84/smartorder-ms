package it.portus.ms.commons.http.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@UtilityClass
public class HttpRequestUtils {
  private static final String FORWARDED_FILTERED_ATTR = "forwardedHeaderFilter.FILTERED";

  public static Optional<String> getForwardedPrefixIfFiltered() {
    return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
        .filter(ServletRequestAttributes.class::isInstance)
        .map(ServletRequestAttributes.class::cast)
        .filter(
            attrs ->
                Boolean.TRUE.equals(
                    attrs.getAttribute(FORWARDED_FILTERED_ATTR, RequestAttributes.SCOPE_REQUEST)))
        .map(ServletRequestAttributes::getRequest)
        .map(HttpServletRequest::getContextPath);
  }
}
