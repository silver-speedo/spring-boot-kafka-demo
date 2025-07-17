package org.example.shared.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {

    try {
      String correlationId =
          Optional.ofNullable(request.getHeader(CORRELATION_ID_HEADER))
              .filter(id -> !id.isBlank())
              .orElse(UUID.randomUUID().toString());

      MDC.put("correlationId", correlationId);

      response.setHeader(CORRELATION_ID_HEADER, correlationId);

      filterChain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }
}
