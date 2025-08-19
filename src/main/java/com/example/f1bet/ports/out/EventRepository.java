package com.example.f1bet.ports.out;

import com.example.f1bet.domain.entity.Event;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {
  Optional<Event> findById(UUID id);

  Event save(Event event);

  Event update(Event event, long expectedVersion);

  List<Event> findPage(int page, int size);

  long versionOf(UUID id);
}
