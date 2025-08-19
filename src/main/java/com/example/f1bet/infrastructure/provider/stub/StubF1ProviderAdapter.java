package com.example.f1bet.infrastructure.provider.stub;

import com.example.f1bet.domain.enums.SessionType;
import com.example.f1bet.ports.out.F1ProviderPort;
import com.example.f1bet.ports.out.ProviderDriver;
import com.example.f1bet.ports.out.ProviderSession;
import com.example.f1bet.ports.out.ProviderSessionFilter;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class StubF1ProviderAdapter implements F1ProviderPort {
  @Override
  public List<ProviderSession> listSessions(ProviderSessionFilter filter) {
    List<ProviderSession> allSessions =
        Arrays.asList(
            new ProviderSession(
                "550e8400-e29b-41d4-a716-446655440001",
                "Australian GP - Race",
                SessionType.RACE,
                "Australia",
                2025,
                Instant.parse("2025-03-16T05:00:00Z")),
            new ProviderSession(
                "550e8400-e29b-41d4-a716-446655440002",
                "Monaco GP - Qualifying",
                SessionType.QUALIFYING,
                "Monaco",
                2025,
                Instant.parse("2025-05-24T14:00:00Z")),
            new ProviderSession(
                "550e8400-e29b-41d4-a716-446655440003",
                "British GP - Practice",
                SessionType.PRACTICE,
                "UK",
                2025,
                Instant.parse("2025-07-04T10:00:00Z")));

    // Apply filtering
    return allSessions.stream()
        .filter(
            session ->
                filter.sessionType() == null || session.sessionType() == filter.sessionType())
        .filter(session -> filter.year() == null || session.year() == filter.year())
        .filter(
            session ->
                filter.country() == null || session.country().equalsIgnoreCase(filter.country()))
        .collect(java.util.stream.Collectors.toList());
  }

  @Override
  public List<ProviderDriver> listDriversForSession(String sessionId) {
    return Arrays.asList(
        new ProviderDriver("d1", "Lewis Hamilton"),
        new ProviderDriver("d2", "Max Verstappen"),
        new ProviderDriver("d3", "Charles Leclerc"));
  }
}
