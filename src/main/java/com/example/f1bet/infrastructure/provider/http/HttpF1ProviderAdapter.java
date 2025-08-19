package com.example.f1bet.infrastructure.provider.http;

import com.example.f1bet.ports.out.F1ProviderPort;
import com.example.f1bet.ports.out.ProviderDriver;
import com.example.f1bet.ports.out.ProviderSession;
import com.example.f1bet.ports.out.ProviderSessionFilter;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@ConditionalOnProperty(name = "app.provider.mode", havingValue = "http")
public class HttpF1ProviderAdapter implements F1ProviderPort {
  private final WebClient webClient;
  private final Duration timeout;

  public HttpF1ProviderAdapter(
      WebClient webClient, @Value("${app.provider.timeout:2s}") String timeout) {
    this.webClient = webClient;
    this.timeout = Duration.parse("PT" + timeout);
  }

  @Override
  public List<ProviderSession> listSessions(ProviderSessionFilter filter) {
    return retry(
            () ->
                webClient
                    .get()
                    .uri(
                        uriBuilder ->
                            uriBuilder
                                .path("/sessions")
                                .queryParam("sessionType", filter.sessionType())
                                .queryParam("year", filter.year())
                                .queryParam("country", filter.country())
                                .build())
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .timeout(timeout)
                    .collectList()
                    .block(),
            3)
        .stream()
        .map(this::mapSession)
        .collect(Collectors.toList());
  }

  @Override
  public List<ProviderDriver> listDriversForSession(String sessionId) {
    return retry(
            () ->
                webClient
                    .get()
                    .uri("/sessions/" + sessionId + "/drivers")
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .timeout(timeout)
                    .collectList()
                    .block(),
            3)
        .stream()
        .map(this::mapDriver)
        .collect(Collectors.toList());
  }

  private ProviderSession mapSession(Map<String, Object> json) {
    // Map JSON to ProviderSession (implement as per actual JSON structure)
    return new ProviderSession(
        (String) json.get("id"),
        (String) json.get("name"),
        com.example.f1bet.domain.enums.SessionType.valueOf((String) json.get("sessionType")),
        (String) json.get("country"),
        (Integer) json.get("year"),
        java.time.Instant.parse((String) json.get("startTime")));
  }

  private ProviderDriver mapDriver(Map<String, Object> json) {
    return new ProviderDriver((String) json.get("id"), (String) json.get("fullName"));
  }

  private <T> T retry(java.util.concurrent.Callable<T> action, int attempts) {
    Exception lastException = null;
    for (int i = 0; i < attempts; i++) {
      try {
        return action.call();
      } catch (WebClientResponseException e) {
        lastException = e;
        if (i == attempts - 1) break;
        try {
          Thread.sleep(100 * (i + 1)); // Simple backoff
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          break;
        }
      } catch (Exception e) {
        lastException = e;
        break;
      }
    }
    throw new RuntimeException("Failed after " + attempts + " attempts", lastException);
  }
}
