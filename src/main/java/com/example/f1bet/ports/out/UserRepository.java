package com.example.f1bet.ports.out;

import com.example.f1bet.domain.entity.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
  Optional<User> findById(UUID id);

  User save(User user);

  User update(User user, long expectedVersion);

  long versionOf(UUID id);
}
