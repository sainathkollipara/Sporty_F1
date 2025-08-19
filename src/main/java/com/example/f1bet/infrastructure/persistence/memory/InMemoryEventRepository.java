package com.example.f1bet.infrastructure.persistence.memory;

import com.example.f1bet.domain.entity.Event;
import com.example.f1bet.ports.out.EventRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryEventRepository implements EventRepository {
  private static final class VersionedEvent {
    final Event event;
    final long version;

    VersionedEvent(Event event, long version) {
      this.event = event;
      this.version = version;
    }
  }

  private final ConcurrentHashMap<UUID, VersionedEvent> store = new ConcurrentHashMap<>();

  @Override
  public Optional<Event> findById(UUID id) {
    VersionedEvent ve = store.get(id);
    return Optional.ofNullable(ve == null ? null : ve.event);
  }

  @Override
  public Event save(Event event) {
    store.put(event.getId(), new VersionedEvent(event, 0L));
    return event;
  }

  @Override
  public Event update(Event event, long expectedVersion) {
    store.compute(
        event.getId(),
        (id, current) -> {
          if (current == null) return new VersionedEvent(event, 0L);
          if (current.version != expectedVersion) {
            throw new OptimisticLockException(
                "Event version mismatch: expected="
                    + expectedVersion
                    + ", actual="
                    + current.version);
          }
          return new VersionedEvent(event, current.version + 1);
        });
    return event;
  }

  @Override
  public List<Event> findPage(int page, int size) {
    if (page < 0 || size <= 0) return List.of();
    List<Event> events = new ArrayList<>();
    for (VersionedEvent ve : store.values()) {
      events.add(ve.event);
    }
    events.sort(Comparator.comparing(Event::getId));
    int from = Math.min(page * size, events.size());
    int to = Math.min(from + size, events.size());
    return events.subList(from, to);
  }

  @Override
  public long versionOf(UUID id) {
    VersionedEvent ve = store.get(id);
    return ve == null ? 0L : ve.version;
  }
}
