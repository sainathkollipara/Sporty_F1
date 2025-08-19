package com.example.f1bet.infrastructure.persistence.memory;

import com.example.f1bet.domain.entity.User;
import com.example.f1bet.ports.out.UserRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {
  private static final class VersionedUser {
    final User user;
    final long version;

    VersionedUser(User user, long version) {
      this.user = user;
      this.version = version;
    }
  }

  private final ConcurrentHashMap<UUID, VersionedUser> store = new ConcurrentHashMap<>();

  @Override
  public Optional<User> findById(UUID id) {
    VersionedUser vu = store.computeIfAbsent(id, key -> new VersionedUser(User.create(key), 0L));
    return Optional.of(vu.user);
  }

  @Override
  public User save(User user) {
    store.put(user.getId(), new VersionedUser(user, 0L));
    return user;
  }

  @Override
  public User update(User user, long expectedVersion) {
    store.compute(
        user.getId(),
        (id, current) -> {
          if (current == null) return new VersionedUser(user, 0L);
          if (current.version != expectedVersion) {
            throw new OptimisticLockException(
                "User version mismatch: expected="
                    + expectedVersion
                    + ", actual="
                    + current.version);
          }
          return new VersionedUser(user, current.version + 1);
        });
    return user;
  }

  @Override
  public long versionOf(UUID id) {
    VersionedUser vu = store.get(id);
    return vu == null ? 0L : vu.version;
  }
}
