package com.example.f1bet.ports.out;

import com.example.f1bet.domain.enums.SessionType;

public record ProviderSessionFilter(SessionType sessionType, Integer year, String country) {}
