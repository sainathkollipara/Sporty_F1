package com.example.f1bet.infrastructure.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class IdempotencyKeyFilter implements Filter {
  private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
  private static final String CACHE_KEY_PREFIX = "POST:/api/v1/bets:";
  private static final long TTL_MILLIS = TimeUnit.MINUTES.toMillis(5);

  private static class CachedResponse {
    final int status;
    final Map<String, String> headers;
    final byte[] body;
    final long expiresAt;

    CachedResponse(int status, Map<String, String> headers, byte[] body, long expiresAt) {
      this.status = status;
      this.headers = headers;
      this.body = body;
      this.expiresAt = expiresAt;
    }

    boolean isExpired() {
      return System.currentTimeMillis() > expiresAt;
    }
  }

  private final ConcurrentHashMap<String, CachedResponse> cache = new ConcurrentHashMap<>();

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (!(request instanceof HttpServletRequest req)
        || !(response instanceof HttpServletResponse res)) {
      chain.doFilter(request, response);
      return;
    }
    if (!"POST".equals(req.getMethod()) || !req.getRequestURI().equals("/api/v1/bets")) {
      chain.doFilter(request, response);
      return;
    }
    String key = req.getHeader(IDEMPOTENCY_KEY_HEADER);
    if (key == null || key.isBlank()) {
      chain.doFilter(request, response);
      return;
    }
    String cacheKey = CACHE_KEY_PREFIX + key;
    CachedResponse cached = cache.get(cacheKey);
    if (cached != null && !cached.isExpired()) {
      res.setStatus(cached.status);
      cached.headers.forEach(res::setHeader);
      res.getOutputStream().write(cached.body);
      return;
    }
    ContentCachingResponseWrapper wrapped = new ContentCachingResponseWrapper(res);
    chain.doFilter(request, wrapped);
    byte[] body = wrapped.getContentAsByteArray();
    Map<String, String> headers =
        wrapped.getHeaderNames().stream()
            .collect(java.util.stream.Collectors.toMap(h -> h, wrapped::getHeader));
    cache.put(
        cacheKey,
        new CachedResponse(
            wrapped.getStatus(), headers, body, System.currentTimeMillis() + TTL_MILLIS));
    wrapped.copyBodyToResponse();
  }
}
