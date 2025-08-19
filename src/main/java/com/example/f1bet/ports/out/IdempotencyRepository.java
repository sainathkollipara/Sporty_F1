package com.example.f1bet.ports.out;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRepository {
  Optional<UUID> findByUserIdAndKey(UUID userId, String key);

  void save(UUID userId, String key, UUID betId);
}
