package com.example.f1bet.ports.out;

import com.example.f1bet.domain.entity.Bet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BetRepository {
  Optional<Bet> findById(UUID id);

  Bet save(Bet bet);

  Bet update(Bet bet, long expectedVersion);

  List<Bet> findByEventId(UUID eventId);

  long versionOf(UUID id);
}
