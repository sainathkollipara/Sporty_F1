package com.example.f1bet.infrastructure.persistence.memory;

import com.example.f1bet.domain.entity.Bet;
import com.example.f1bet.ports.out.BetRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryBetRepository implements BetRepository {
  private static final class VersionedBet {
    final Bet bet;
    final long version;

    VersionedBet(Bet bet, long version) {
      this.bet = bet;
      this.version = version;
    }
  }

  private final ConcurrentHashMap<UUID, VersionedBet> store = new ConcurrentHashMap<>();
  private final AtomicLong idSeq = new AtomicLong(1);

  @Override
  public Optional<Bet> findById(UUID id) {
    VersionedBet vb = store.get(id);
    return Optional.ofNullable(vb == null ? null : vb.bet);
  }

  @Override
  public Bet save(Bet bet) {
    // assumes bet already has an id assigned by caller; otherwise, generate here if needed
    store.put(bet.getId(), new VersionedBet(bet, 0L));
    return bet;
  }

  @Override
  public Bet update(Bet bet, long expectedVersion) {
    store.compute(
        bet.getId(),
        (id, current) -> {
          if (current == null) return new VersionedBet(bet, 0L);
          if (current.version != expectedVersion) {
            throw new OptimisticLockException(
                "Bet version mismatch: expected="
                    + expectedVersion
                    + ", actual="
                    + current.version);
          }
          return new VersionedBet(bet, current.version + 1);
        });
    return bet;
  }

  public UUID nextId() {
    return new UUID(0L, idSeq.getAndIncrement());
  }

  @Override
  public List<Bet> findByEventId(UUID eventId) {
    List<Bet> list = new ArrayList<>();
    for (VersionedBet vb : store.values()) {
      if (vb.bet.getEventId().equals(eventId)) {
        list.add(vb.bet);
      }
    }
    return list;
  }

  @Override
  public long versionOf(UUID id) {
    VersionedBet vb = store.get(id);
    return vb == null ? 0L : vb.version;
  }
}
