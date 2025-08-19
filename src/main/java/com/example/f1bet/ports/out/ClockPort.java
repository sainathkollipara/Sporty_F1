package com.example.f1bet.ports.out;

import java.time.Instant;

public interface ClockPort {
  Instant now();
}
