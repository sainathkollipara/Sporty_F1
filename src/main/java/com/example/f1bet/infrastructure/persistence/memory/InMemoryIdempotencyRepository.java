package com.example.f1bet.infrastructure.persistence.memory;

import com.example.f1bet.ports.out.IdempotencyRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryIdempotencyRepository implements IdempotencyRepository {
  private final Map<String, UUID> map = new ConcurrentHashMap<>();

  private String key(UUID userId, String idempotencyKey) {
    return userId.toString() + ":" + idempotencyKey;
  }

  @Override
  public Optional<UUID> findByUserIdAndKey(UUID userId, String key) {
    if (key == null) return Optional.empty();
    return Optional.ofNullable(map.get(key(userId, key)));
  }

  @Override
  public void save(UUID userId, String key, UUID betId) {
    if (key == null) return;
    map.putIfAbsent(key(userId, key), betId);
  }
}
