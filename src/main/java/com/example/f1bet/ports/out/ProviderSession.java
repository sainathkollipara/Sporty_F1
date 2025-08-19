package com.example.f1bet.ports.out;

import com.example.f1bet.domain.enums.SessionType;
import java.time.Instant;

public record ProviderSession(
    String id, String name, SessionType sessionType, String country, int year, Instant startTime) {}
