package com.example.f1bet.application.service;

import com.example.f1bet.domain.entity.Bet;
import com.example.f1bet.domain.entity.Event;
import com.example.f1bet.domain.entity.User;
import com.example.f1bet.domain.exception.InsufficientBalanceException;
import com.example.f1bet.domain.exception.InvalidBetException;
import com.example.f1bet.domain.vo.Money;
import com.example.f1bet.ports.out.BetRepository;
import com.example.f1bet.ports.out.EventRepository;
import com.example.f1bet.ports.out.IdempotencyRepository;
import com.example.f1bet.ports.out.UserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlaceBetService {

  public record Command(
      UUID userId,
      UUID eventId,
      UUID selectionId,
      BigDecimal stake,
      String currency,
      String idempotencyKey) {}

  private final UserRepository users;
  private final EventRepository events;
  private final BetRepository bets;
  private final IdempotencyRepository idempotency;

  // package-private constructor for tests
  PlaceBetService(UserRepository users, EventRepository events, BetRepository bets) {
    this(
        users,
        events,
        bets,
        new com.example.f1bet.infrastructure.persistence.memory.InMemoryIdempotencyRepository());
  }

  @Autowired
  public PlaceBetService(
      UserRepository users,
      EventRepository events,
      BetRepository bets,
      IdempotencyRepository idempotency) {
    this.users = users;
    this.events = events;
    this.bets = bets;
    this.idempotency = idempotency;
  }

  public Bet place(Command cmd) {
    // check idempotency mapping first
    if (cmd.idempotencyKey() != null && !cmd.idempotencyKey().isBlank()) {
      Optional<UUID> existing = idempotency.findByUserIdAndKey(cmd.userId(), cmd.idempotencyKey());
      if (existing.isPresent()) {
        // if previous bet exists, return the saved Bet from repository if possible
        return bets.findById(existing.get())
            .orElseThrow(() -> new InvalidBetException("Idempotent bet not found"));
      }
    }

    Event event =
        events
            .findById(cmd.eventId())
            .orElseThrow(() -> new InvalidBetException("Event not found"));
    if (!event.hasSelection(cmd.selectionId())) {
      throw new InvalidBetException("Selection not part of the event");
    }
    Money stake = Money.stake(cmd.currency(), cmd.stake());

    User user = users.findById(cmd.userId()).orElseGet(() -> users.save(User.create(cmd.userId())));

    if (user.getBalance().getAmount().compareTo(stake.getAmount()) < 0
        || !user.getBalance().getCurrency().equals(stake.getCurrency())) {
      throw new InsufficientBalanceException("Insufficient balance");
    }

    UUID betId = UUID.randomUUID();
    Bet bet =
        new Bet(
            betId,
            cmd.userId(),
            cmd.eventId(),
            cmd.selectionId(),
            stake,
            event.getMarket().getSelections().stream()
                .filter(s -> s.getId().equals(cmd.selectionId()))
                .findFirst()
                .orElseThrow()
                .getOdds(),
            event,
            0L);

    bets.save(bet);
    long userVersion = users.versionOf(user.getId());
    users.update(user.withBalance(user.getBalance().subtract(stake)), userVersion);

    // persist idempotency mapping after successful save
    if (cmd.idempotencyKey() != null && !cmd.idempotencyKey().isBlank()) {
      idempotency.save(cmd.userId(), cmd.idempotencyKey(), betId);
    }

    return bet;
  }
}
